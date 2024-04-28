/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.component;

import com.intellij.icons.AllIcons;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.ui.HyperlinkLabel;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifact;
import com.microsoft.azure.toolkit.intellij.common.AzureArtifactComboBox;
import com.microsoft.azure.toolkit.intellij.common.AzureFormJPanel;
import com.microsoft.azure.toolkit.intellij.common.EnvironmentVariablesTextFieldWithBrowseButton;
import com.microsoft.azure.toolkit.lib.common.form.AzureFormInput;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.event.ItemListener;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArtifactForm implements AzureFormJPanel<ContainerAppDraft.ImageConfig>, DeploymentSourceForm {
    private static final String LINK_SUPPORTED_JAVA_BUILD_ENV = "https://learn.microsoft.com/en-us/azure/container-apps/java-build-environment-variables?source=recommendations#supported-java-build-environment-variables";
    private final Project project;
    @Getter
    private JPanel contentPanel;
    private JLabel lblArtifact;
    private AzureArtifactComboBox selectorArtifact;
    private EnvironmentVariablesTextFieldWithBrowseButton inputEnv;
    private HyperlinkLabel buildEnvLink;

    @Setter
    @Getter
    private ContainerApp containerApp;

    public ArtifactForm(final Project project) {
        super();
        this.project = project;
        $$$setupUI$$$(); // tell IntelliJ to call createUIComponents() here.
        this.init();
    }

    private void init() {
        this.selectorArtifact.setLabel("Artifact");
        this.selectorArtifact.setRequired(true);
        this.selectorArtifact.setFileFilter(virtualFile -> {
            final String ext = FileNameUtils.getExtension(virtualFile.getPath());
            return StringUtils.equalsAnyIgnoreCase(ext, "jar", "war");
        });
        this.lblArtifact.setLabelFor(selectorArtifact);
        this.lblArtifact.setIcon(AllIcons.General.ContextHelp);
        buildEnvLink.setHtmlText(String.format("<html><a href=\"%s\">Supported Java build environment variables</a></html>", LINK_SUPPORTED_JAVA_BUILD_ENV));
        buildEnvLink.addHyperlinkListener((e) -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                BrowserUtil.browse(LINK_SUPPORTED_JAVA_BUILD_ENV);
            }
        });
        buildEnvLink.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    @Override
    public ContainerAppDraft.ImageConfig getValue() {
        final String fullImageName = this.getDefaultFullImageName();
        final ContainerAppDraft.ImageConfig config = new ContainerAppDraft.ImageConfig(fullImageName);
        final ContainerAppDraft.BuildImageConfig buildConfig = new ContainerAppDraft.BuildImageConfig();
        Optional.ofNullable(selectorArtifact.getValue()).map(AzureArtifact::getFileForDeployment)
            .map(Path::of)
            .filter(Files::exists)
            .ifPresent(buildConfig::setSource);
        final Map<String, String> envVarsMap = this.inputEnv.getEnvironmentVariables();
        buildConfig.setSourceBuildEnv(envVarsMap);
        config.setBuildImageConfig(buildConfig);
        return config;
    }

    @Override
    public void setValue(final ContainerAppDraft.ImageConfig config) {
        Optional.ofNullable(config.getBuildImageConfig()).ifPresent(buildConfig -> {
            Optional.of(buildConfig.getSource())
                .map(f -> AzureArtifact.createFromFile(f.toAbsolutePath().toString(), project))
                .ifPresent(selectorArtifact::setValue);
            Optional.ofNullable(buildConfig.getSourceBuildEnv()).ifPresent(this.inputEnv::setEnvironmentVariables);
        });
    }

    @Override
    public List<AzureFormInput<?>> getInputs() {
        return Collections.singletonList(this.selectorArtifact);
    }

    @Override
    public void setVisible(final boolean visible) {
        this.contentPanel.setVisible(visible);
    }

    public void setFixedArtifact(final AzureArtifact artifact){
        this.lblArtifact.setVisible(false);
        this.selectorArtifact.setVisible(false);
        this.selectorArtifact.setArtifact(artifact);
    }

    private void createUIComponents() {
        this.selectorArtifact = new AzureArtifactComboBox(project, true);
        this.selectorArtifact.reloadItems();
    }

    public void setModule(Module module) {
        selectorArtifact.setModule(module);
        selectorArtifact.setFileArtifactOnly(false);
        selectorArtifact.reloadItems();
    }

    public void addArtifactListener(@Nonnull final ItemListener listener) {
        this.selectorArtifact.addItemListener(listener);
    }

    public AzureArtifact getArtifact() {
        return this.selectorArtifact.getValue();
    }
}
