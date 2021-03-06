package edu.princeton.safe.internal.cytoscape;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import edu.princeton.safe.internal.cytoscape.controller.AnnotationChooserController;
import edu.princeton.safe.internal.cytoscape.controller.AttributeBrowserController;
import edu.princeton.safe.internal.cytoscape.controller.CompositeMapController;
import edu.princeton.safe.internal.cytoscape.controller.DomainBrowserController;
import edu.princeton.safe.internal.cytoscape.controller.ImportPanelController;
import edu.princeton.safe.internal.cytoscape.controller.SafeController;
import edu.princeton.safe.internal.cytoscape.event.DefaultEventService;
import edu.princeton.safe.internal.cytoscape.event.EventService;
import edu.princeton.safe.internal.cytoscape.io.SafeSessionSerializer;

public class CyActivator extends AbstractCyActivator {

    @Override
    public void start(BundleContext context) throws Exception {

        CyApplicationManager applicationManager = getService(context, CyApplicationManager.class);
        CyNetworkViewManager networkViewManager = getService(context, CyNetworkViewManager.class);
        CyServiceRegistrar registrar = getService(context, CyServiceRegistrar.class);
        CySwingApplication application = getService(context, CySwingApplication.class);
        DialogTaskManager taskManager = getService(context, DialogTaskManager.class);
        CyTableManager tableManager = getService(context, CyTableManager.class);
        CyTableFactory tableFactory = getService(context, CyTableFactory.class);

        VisualMappingManager visualMappingManager = getService(context, VisualMappingManager.class);
        VisualStyleFactory visualStyleFactory = getService(context, VisualStyleFactory.class);

        VisualMappingFunctionFactory continuousMappingFactory = getService(context, VisualMappingFunctionFactory.class,
                                                                           "(mapping.type=continuous)");

        VisualMappingFunctionFactory passthroughMappingFactory = getService(context, VisualMappingFunctionFactory.class,
                                                                            "(mapping.type=passthrough)");

        StyleFactory styleFactory = new StyleFactory(visualStyleFactory, continuousMappingFactory,
                                                     passthroughMappingFactory);

        EventService eventService = new DefaultEventService();

        AttributeBrowserController attributeBrowser = new AttributeBrowserController(visualMappingManager, styleFactory,
                                                                                     eventService, taskManager);

        AnnotationChooserController annotationChooser = new AnnotationChooserController(application, taskManager);

        ImportPanelController importPanel = new ImportPanelController(taskManager, attributeBrowser, annotationChooser,
                                                                      eventService);

        DomainBrowserController domainBrowser = new DomainBrowserController(visualMappingManager, styleFactory,
                                                                            taskManager, eventService);

        CompositeMapController compositeMapPanel = new CompositeMapController(taskManager, domainBrowser, eventService);

        SafeSessionSerializer serializer = new SafeSessionSerializer(tableManager, tableFactory);

        SelectionTracker selectionTracker = new SelectionTracker(eventService);

        RedrawTracker redrawTracker = new RedrawTracker(eventService);

        SafeController safeController = new SafeController(registrar, application, applicationManager, importPanel,
                                                           attributeBrowser, compositeMapPanel, domainBrowser,
                                                           eventService, serializer, selectionTracker);

        Map<String, String> safeActionProperties = new MapBuilder().put("inMenuBar", "true")
                                                                   .put("preferredMenu", ServiceProperties.APPS_MENU)
                                                                   .build();
        SafeAction safeAction = new SafeAction(safeActionProperties, applicationManager, networkViewManager,
                                               safeController);
        safeAction.putValue(CyAction.NAME, "SAFE");

        registerService(context, safeAction, CyAction.class);
        registerService(context, safeController, SetCurrentNetworkViewListener.class,
                        NetworkViewAboutToBeDestroyedListener.class, ColumnCreatedListener.class,
                        ColumnDeletedListener.class, ColumnNameChangedListener.class, SessionLoadedListener.class,
                        SessionAboutToBeSavedListener.class);
        registerService(context, selectionTracker, RowsSetListener.class);
        registerService(context, redrawTracker, UpdateNetworkPresentationListener.class);
    }

    void registerService(BundleContext context,
                         Object service,
                         Class<?>... interfaces) {
        for (Class<?> type : interfaces) {
            registerService(context, service, type, new Properties());
        }
    }

    static class PropertiesBuilder {
        Properties properties;

        public PropertiesBuilder() {
            properties = new Properties();
        }

        public PropertiesBuilder put(String key,
                                     Object value) {
            properties.put(key, value);
            return this;
        }

        public Properties build() {
            return properties;
        }
    }

    static class MapBuilder {
        Map<String, String> map;

        public MapBuilder() {
            map = new HashMap<>();
        }

        public MapBuilder put(String key,
                              String value) {
            map.put(key, value);
            return this;
        }

        public Map<String, String> build() {
            return map;
        }
    }
}
