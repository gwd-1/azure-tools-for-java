/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package com.microsoft.azure.toolkit.intellij.containerapps.updateimage;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.microsoft.azure.toolkit.ide.containerregistry.ContainerRegistryActionsContributor;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.exception.AzureToolkitRuntimeException;
import com.microsoft.azure.toolkit.lib.common.task.AzureTaskManager;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerApp;
import com.microsoft.azure.toolkit.lib.containerapps.containerapp.ContainerAppDraft;
import com.microsoft.azure.toolkit.lib.containerregistry.ContainerRegistry;
import com.microsoft.azure.toolkit.lib.containerregistry.Tag;

import java.util.Objects;

public class UpdateContainerImageAction {
    public static void openUpdateDialog(ContainerApp app, AnActionEvent e) {
        AzureTaskManager.getInstance().runLater(() -> {
            final UpdateImageDialog dialog = new UpdateImageDialog(e.getProject());
            if (Objects.nonNull(app)) {
                dialog.getForm().setApp(app);
            }
            final Action<UpdateImageForm.UpdateImageConfig> okAction = getUpdateImageAction();
            dialog.setOkAction(okAction);
            dialog.show();
        });
    }

    public static void openUpdateDialog(Tag tag, AnActionEvent e) {
        final ContainerRegistry registry = tag.getParent().getParent().getParent();
        if (!registry.isAdminUserEnabled()) {
            final Action<ContainerRegistry> enableAdminUser = AzureActionManager.getInstance().getAction(ContainerRegistryActionsContributor.ENABLE_ADMIN_USER).bind(registry);
            throw new AzureToolkitRuntimeException(String.format("Admin user is not enabled for Azure Container Registry (%s).", registry.getName()), enableAdminUser);
        }
        AzureTaskManager.getInstance().runLater(() -> {
            final UpdateImageDialog dialog = new UpdateImageDialog(e.getProject());
            final UpdateImageForm.UpdateImageConfig config = new UpdateImageForm.UpdateImageConfig();
            final ContainerAppDraft.ImageConfig imageConfig = new ContainerAppDraft.ImageConfig(tag.getFullName());
            imageConfig.setContainerRegistry(registry);
            config.setImage(imageConfig);
            dialog.getForm().setValue(config);
            final Action<UpdateImageForm.UpdateImageConfig> okAction = getUpdateImageAction();
            dialog.setOkAction(okAction);
            dialog.show();
        });
    }

    private static Action<UpdateImageForm.UpdateImageConfig> getUpdateImageAction() {
        return new Action<UpdateImageForm.UpdateImageConfig>(Action.Id.of("user/containerapps.update_image.app"))
            .withLabel("Update")
            .withIdParam(c -> c.getApp().getName())
            .withAuthRequired(true)
            .withSource(UpdateImageForm.UpdateImageConfig::getApp)
            .withHandler(c -> {
                final ContainerAppDraft draft = (ContainerAppDraft) c.getApp().update();
                final ContainerAppDraft.Config config = new ContainerAppDraft.Config();
                config.setImageConfig(c.getImage());
                draft.setConfig(config);
                draft.updateIfExist();
            });
    }
}
