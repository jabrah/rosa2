package rosa.website.core.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import rosa.archive.core.Store;
import rosa.search.core.SearchService;
import rosa.search.tool.Tool;

public class IndexToolMain {

    public static void main(String[] args) {
        Injector injector = Guice.createInjector(new ToolModule());

        Tool tool = new Tool(
                injector.getInstance(Store.class),
                injector.getInstance(SearchService.class),
                System.out
        );

        tool.process(args);
    }

}
