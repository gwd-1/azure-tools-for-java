/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.appservice.task;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceId;
import com.intellij.execution.RunManagerEx;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.microsoft.azure.toolkit.ide.guidance.ComponentContext;
import com.microsoft.azure.toolkit.ide.guidance.task.BaseDeployTask;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.AzureFunctionSupportConfigurationType;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.core.FunctionUtils;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.FunctionDeployConfiguration;
import com.microsoft.azure.toolkit.intellij.legacy.function.runner.deploy.FunctionDeploymentConfigurationFactory;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.appservice.function.AzureFunctions;
import com.microsoft.azure.toolkit.lib.appservice.function.FunctionApp;
import com.microsoft.azure.toolkit.lib.appservice.utils.AppServiceConfigUtils;
import com.microsoft.azure.toolkit.lib.common.utils.Utils;

import javax.annotation.Nonnull;
import java.util.Objects;

public class DeployFunctionAppTask extends BaseDeployTask {
    private final AzureFunctionSupportConfigurationType functionType = AzureFunctionSupportConfigurationType.getInstance();

    public DeployFunctionAppTask(@Nonnull ComponentContext context) {
        super(context);
    }

    @Override
    protected RunnerAndConfigurationSettings getRunConfigurationSettings(@Nonnull ComponentContext context, RunManagerEx manager) {
        final String appId = (String) context.getParameter("functionId");
        final ConfigurationFactory factory = new FunctionDeploymentConfigurationFactory(functionType);
        final String runConfigurationName = String.format("Azure Sample: %s-%s", guidance.getName(), Utils.getTimestamp());
        final RunnerAndConfigurationSettings settings = manager.createConfiguration(runConfigurationName, factory);
        final RunConfiguration runConfiguration = settings.getConfiguration();
        if (runConfiguration instanceof FunctionDeployConfiguration) {
            final Module[] functionModules = FunctionUtils.listFunctionModules(project);
            ((FunctionDeployConfiguration) runConfiguration).saveTargetModule(functionModules[0]);
            final FunctionApp functionApp = Objects.requireNonNull(Azure.az(AzureFunctions.class).functionApp(appId),
                    String.format("Can not find target app %s, please check whether app is created successfully", ResourceId.fromString(appId).name()));
            ((FunctionDeployConfiguration) runConfiguration).saveConfig(AppServiceConfigUtils.fromFunctionApp(functionApp));
        }
        return settings;
    }

    @Nonnull
    @Override
    public String getName() {
        return "task.function.deploy";
    }
}
