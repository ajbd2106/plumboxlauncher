package in.oogway.plumbox.cli;

import in.oogway.plumbox.launcher.Ingester;
import in.oogway.plumbox.launcher.Pipeline;
import in.oogway.plumbox.launcher.Source;
import in.oogway.plumbox.launcher.storage.LauncherStorageDriver;
import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;

public class Cli {
  private static HashMap<String, CliHandler> handlers = new HashMap<>();
  private static Options options = new Options();
  private static CommandLineParser bparser = new DefaultParser();

  static {
    Option opt = Option.builder("P").hasArgs().build();
    options.addOption(opt);

    handlers.put("declare-ingester", (Plumbox pb, HashMap<String, String> ns) -> {
      String transformation = ns.get("pipeline");
      Pipeline t = (Pipeline) pb.get(transformation, Pipeline.class);
      if (t == null) {
        throw new RuntimeException(String.format("%s not found", transformation));
      }

      pb.declare(new Ingester(
          ns.get("source"),
          ns.get("sink"),
          ns.get("pipeline")));
    });

    handlers.put("declare-pipeline", (Plumbox pb, HashMap<String, String> ns) -> {
      pb.declare(new Pipeline(ns.get("stages").split(",")));
    });

    ArrayList<Class> getters = new ArrayList<Class>();
    getters.add(Pipeline.class);
    getters.add(Ingester.class);

    for (Class entity: getters) {
      addGetter(entity);
      addGetAll(entity);
    }
  }

  private static <T> void addGetter(Class<T> entity) {
    String handlerOne = String.format("get-%s", entity.getSimpleName().toLowerCase());
    handlers.put(handlerOne, (Plumbox pb, HashMap<String, String> ns) -> {
      System.out.println(pb.get(ns.get("id"), entity));
    });
  }

  private static <T> void addGetAll(Class<T> entity) {
    String handlerAll = String.format("get-%ss", entity.getSimpleName().toLowerCase());
    handlers.put(handlerAll, (Plumbox pb, HashMap<String, String> ns) -> {
      pb.getAll(entity.getSimpleName().toLowerCase(), entity)
          .forEach((k, n) ->
              System.out.println(String.format("%s -> %s", k, n)));
    });
  }

  public static void execute(String[] args, LauncherStorageDriver driver) throws Exception {
    if (args.length < 2) {
      System.out.println(String.format("Must provide a subcommand"));
      System.exit(127);
    }

    String cmd = args[0];
    CliHandler handler = handlers.get(cmd);
    if (handler == null) {
      System.out.println(String.format("No matching handler for %s", cmd));
      System.exit(127);
    }

    CommandLine line = bparser.parse(options, args);
    HashMap<String, String> options = new HashMap<>();
    Properties ns = line.getOptionProperties("P");
    Enumeration<?> d = ns.propertyNames();

    while (d.hasMoreElements()) {
      Object val = d.nextElement();
      options.put((String) val, (String) ns.get(val));
    }

    Plumbox pb = new Plumbox(driver);
    handler.run(pb, options);
  }
}
