package com.microsoft.azure.toolkit.intellij.azuresdk;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.microsoft.azure.toolkit.ide.common.action.ResourceCommonActionsContributor;
import com.microsoft.azure.toolkit.intellij.azuresdk.service.MachineTaggingService;
import com.microsoft.azure.toolkit.intellij.azuresdk.service.WorkspaceTaggingService;
import com.microsoft.azure.toolkit.intellij.common.messager.IntellijAzureMessage;
import com.microsoft.azure.toolkit.lib.AzService;
import com.microsoft.azure.toolkit.lib.Azure;
import com.microsoft.azure.toolkit.lib.common.action.Action;
import com.microsoft.azure.toolkit.lib.common.action.AzureActionManager;
import com.microsoft.azure.toolkit.lib.common.bundle.AzureString;
import com.microsoft.azure.toolkit.lib.common.messager.AzureMessager;
import com.microsoft.azure.toolkit.lib.common.messager.IAzureMessage;
import com.microsoft.azure.toolkit.lib.common.operation.AzureOperation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureAdvertisementService {

    private static final Map<String, String> MACHINE_SERVICES = new LinkedHashMap<>() {{
        put("docker", "Microsoft.ContainerRegistry");
        put("kubectl", "Microsoft.ContainerService");
        put("podman", "Microsoft.ContainerService");

        put("mysql", "Microsoft.DBforMySQL");
        put("psql", "Microsoft.DBforPostgreSQL");
        put("sqlservr", "Microsoft.Sql");
        put("redis-server", "Microsoft.Cache");
        put("redis-cli", "Microsoft.Cache");
        put("azurite", "Microsoft.Storage");
        put("Microsoft.Azure.Cosmos.Emulator", "Microsoft.DocumentDB");
    }};


    private static final Map<String, String> PROJECT_SERVICES = new LinkedHashMap<>() {{
        put("openai", "Microsoft.CognitiveServices");
        put("azure_openai", "Microsoft.CognitiveServices");
        put("redis", "Microsoft.Cache");
        put("eventhubs", "Microsoft.EventHub");
        put("servicebus", "Microsoft.ServiceBus");
        put("azure_storage", "Microsoft.Storage");
        put("cosmos", "Microsoft.DocumentDB");
        put("functions", "Microsoft.Web");
    }};
    public static final String MSG_TEMPLATE = "Current project is detected as possibly using %s. You can managed them in %s after signing-in";

    public static void advertiseProjectService(@Nonnull final Project project) {
        final String service = getNextProjectAdService(project);
        if (service != null) {
            doAdvertise(project, service);
        }
    }

    @AzureOperation("auto/sdk.advertise_service")
    private static void doAdvertise(@Nonnull Project project, String service) {
        final IntellijAzureMessage message = (IntellijAzureMessage) buildMessage(service);
        if (message != null) {
            message.setProject(project).setPriority(IntellijAzureMessage.PRIORITY_HIGH).show(AzureMessager.getMessager());
            PropertiesComponent.getInstance().setValue("azure.advertisement.project.service", service);
        }
    }

    @Nullable
    private static IAzureMessage buildMessage(@Nonnull String service) {
        final Action<Object> signInAction = AzureActionManager.getInstance().getAction(Action.SIGN_IN);

        final Action<Object> focusService = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.SELECT_RESOURCE_IN_EXPLORER);
        final Action<Object> openExplorer = AzureActionManager.getInstance().getAction(ResourceCommonActionsContributor.OPEN_AZURE_EXPLORER);
        final List<AzService> services = Azure.getServices(service);
        final Action<Object> openExplorerAction = services.isEmpty() ? openExplorer : focusService.bind(services.get(0)).withLabel("Open in Azure Explorer");

        return switch (service) {
            case "Microsoft.CognitiveServices" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + ", and try your own \"%s\" with \"%s\" model deployments in the integrated %s. <a href='https://azure.microsoft.com/en-us/products/ai-services/openai-service?_ijop_=openai.learn_more'>Learn more</a> about Azure OpenAI service.",
                    "Azure OpenAI service", "Azure Explorer", "Copilot", "GPT*", "AI playground"), openExplorerAction, signInAction);
            case "Microsoft.Cache" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + ", and explore or manage cached data in the integrated %s. <a href='https://azure.microsoft.com/en-us/products/cache/?_ijop_=redis.learn_more'>Learn more</a> about Azure Cache for Redis.",
                    "Azure Cache for Redis", "Azure Explorer", "Redis Explorer"), openExplorerAction, signInAction);
            case "Microsoft.EventHub" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + ", and send messages to or monitor messages with the integrated %s. <a href='https://azure.microsoft.com/en-us/products/event-hubs?_ijop_=eventhubs.learn_more'>Learn more</a> about Azure Event Hubs.",
                    "Azure Event Hubs", "Azure Explorer", "Event Hub Explorer"), openExplorerAction, signInAction);
            case "Microsoft.ServiceBus" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + ", and send messages to or monitor messages with the integrated %s. <a href='https://azure.microsoft.com/en-us/products/service-bus/?_ijop_=servicebus.learn_more'>Learn more</a> about Azure Service Bus Messaging.",
                    "Azure Service Bus Messaging", "Azure Explorer", "Service Bus Explorer"), openExplorerAction, signInAction);
            case "Microsoft.Storage" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + " with rich functions for browsing and management of blobs and files. <a href='https://azure.microsoft.com/en-us/products/storage/blobs/?_ijop_=storage.learn_more'>Learn more</a> about Azure Storage.",
                    "Azure Storage", "Azure Explorer"), openExplorerAction, signInAction);
            case "Microsoft.DocumentDB" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + " with rich functions for browsing and management of documents. <a href='https://azure.microsoft.com/en-us/products/cosmos-db?_ijop_=cosmos.learn_more'>Learn more</a> about Azure Cosmos DB.",
                    "Azure Cosmos DB", "Azure Explorer"), openExplorerAction, signInAction);
            case "Microsoft.Web" -> AzureMessager.getMessager().buildInfoMessage(
                AzureString.format(MSG_TEMPLATE + " with rich features for debugging, streaming logs, and browsing online files. Learn more about <a href='https://azure.microsoft.com/en-us/products/functions?_ijop_=function.learn_more'>Azure Functions</a>/<a href='https://azure.microsoft.com/en-us/products/app-service/web?_ijop_=webapp.learn_more'>App Service</a>.",
                    "Azure Functions/App Service", "Azure Explorer"), openExplorerAction, signInAction);
            default -> null;
        };
    }

    @Nonnull
    public static List<String> getProjectServices(Project project) {
        final Set<String> tags = WorkspaceTaggingService.getWorkspaceTags(project);
        return PROJECT_SERVICES.entrySet().stream()
            .filter(e -> tags.contains(e.getKey()))
            .map(Map.Entry::getValue).toList();
    }

    @Nonnull
    public static List<String> getMachineServices() {
        final Set<String> tags = MachineTaggingService.getMachineTags();
        return MACHINE_SERVICES.entrySet().stream()
            .filter(e -> tags.contains(e.getKey()))
            .map(Map.Entry::getValue).toList();
    }

    @Nullable
    public static String getNextProjectAdService(@Nonnull final Project project) {
        final List<String> services = getProjectServices(project);
        if (!services.isEmpty()) {
            final String lastService = PropertiesComponent.getInstance().getValue("azure.advertisement.project.service", "");
            final int index = services.indexOf(lastService);
            return services.get((index + 1) % services.size());
        }
        return null;
    }

    @Nullable
    public static String getNextMachineAdService() {
        final List<String> services = getMachineServices();
        if (!services.isEmpty()) {
            final String lastService = PropertiesComponent.getInstance().getValue("azure.advertisement.machine.service", "");
            final int index = services.indexOf(lastService);
            return services.get((index + 1) % services.size());
        }
        return null;
    }
}
