package com.microsoft.azure.toolkit.intellij.connector.dotazure;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunManagerListener;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.impl.ConfigurationSettingsEditorWrapper;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.intellij.common.runconfig.IWebAppRunConfiguration;
import com.microsoft.azure.toolkit.intellij.connector.Connection;
import com.microsoft.azure.toolkit.intellij.connector.ConnectionTopics;
import com.microsoft.azure.toolkit.lib.common.messager.ExceptionNotification;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;
import com.microsoft.intellij.util.BuildArtifactBeforeRunTaskUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BeforeRunTaskAdder implements RunManagerListener, ConnectionTopics.ConnectionChanged, IWebAppRunConfiguration.ModuleChangedListener {
    @Override
    @ExceptionNotification
    public void runConfigurationAdded(@Nonnull RunnerAndConfigurationSettings settings) {
        final RunConfiguration config = settings.getConfiguration();
        final Environment environment = AzureModule.createIfSupport(config).map(AzureModule::getDefaultEnvironment).orElse(null);
        if (Objects.isNull(environment)) {
            return;
        }
        final List<Connection<?, ?>> connections = environment.getConnections();
        final List<BeforeRunTask<?>> tasks = config.getBeforeRunTasks();
        if (connections.stream().anyMatch(c -> c.isApplicableFor(config)) && tasks.stream().noneMatch(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask)) {
            config.getBeforeRunTasks().add(new DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask(config));
        }
    }

    @Override
    public void runConfigurationChanged(@Nonnull RunnerAndConfigurationSettings settings) {
        this.artifactMayChanged(settings.getConfiguration(), null);
    }

    @Override
    @ExceptionNotification
    @AzureOperation(name = "boundary/connector.update_connection_task")
    public void connectionChanged(Project project, Connection<?, ?> connection, ConnectionTopics.Action change) {
        final RunManagerEx rm = RunManagerEx.getInstanceEx(project);
        final List<RunConfiguration> configurations = rm.getAllConfigurationsList();
        for (final RunConfiguration config : configurations) {
            final List<BeforeRunTask<?>> tasks = config.getBeforeRunTasks();
            if (change == ConnectionTopics.Action.ADD) {
                if (connection.isApplicableFor(config) && tasks.stream().noneMatch(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask)) {
                    tasks.add(new DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask(config));
                }
            } else {
                final List<Connection<?, ?>> connections = AzureModule.list(project).stream().map(AzureModule::getDefaultEnvironment).filter(Objects::nonNull)
                        .map(env -> env.getConnectionManager(false))
                        .filter(Objects::nonNull)
                        .map(ConnectionManager::getConnections)
                        .flatMap(List::stream).toList();
                if (connections.stream().noneMatch(c -> c.isApplicableFor(config))) {
                    tasks.removeIf(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask);
                }
            }
        }
    }

    @Override
    @ExceptionNotification
    public void artifactMayChanged(@Nonnull RunConfiguration config, @Nullable ConfigurationSettingsEditorWrapper editor) {
        final List<Connection<?, ?>> connections = AzureModule.createIfSupport(config).map(AzureModule::getDefaultEnvironment)
                .map(env -> env.getConnectionManager(false))
                .map(ConnectionManager::getConnections)
                .orElse(Collections.emptyList());
        final List<BeforeRunTask<?>> tasks = config.getBeforeRunTasks();
        Optional.ofNullable(editor).ifPresent(e -> BuildArtifactBeforeRunTaskUtils.removeTasks(e, (t) -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask));
        tasks.removeIf(t -> t instanceof DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask);
        if (connections.stream().anyMatch(c -> c.isApplicableFor(config))) {
            final List<BeforeRunTask> newTasks = new ArrayList<>(tasks);
            final DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask task = new DotEnvBeforeRunTaskProvider.LoadDotEnvBeforeRunTask(config);
            newTasks.add(task);
            RunManagerEx.getInstanceEx(config.getProject()).setBeforeRunTasks(config, newTasks);
            Optional.ofNullable(editor).ifPresent(e -> e.addBeforeLaunchStep(task));
        }
    }
}
