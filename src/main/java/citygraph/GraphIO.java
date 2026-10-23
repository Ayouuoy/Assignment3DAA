
package citygraph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.StringTokenizer;

public final class GraphIO {

    private GraphIO() {}

    public static Graph read(Path path) throws IOException {
        try (BufferedReader reader =
                     Files.newBufferedReader(path)) {

            String line = reader.readLine();

            if (line == null) {
                throw new IOException("empty graph file");
            }

            StringTokenizer header =
                    new StringTokenizer(line);

            if (header.countTokens() != 3) {
                throw new IOException(
                        "Expected: n m directed|undirected"
                );
            }

            int n;
            int m;

            try {
                n = Integer.parseInt(header.nextToken());
                m = Integer.parseInt(header.nextToken());
            } catch (NumberFormatException ex) {
                throw new IOException("invalid graph size", ex);
            }

            String type = header.nextToken();

            if (!type.equals("directed")
                    && !type.equals("undirected")) {
                throw new IOException("invalid graph type");
            }

            if (n < 0 || m < 0) {
                throw new IOException("negative size");
            }

            Graph graph = new Graph(
                    n,
                    type.equals("directed"),
                    m
            );

            for (int i = 0; i < m; i++) {
                line = reader.readLine();

                if (line == null) {
                    throw new IOException("missing edge " + i);
                }

                StringTokenizer tokens =
                        new StringTokenizer(line);

                if (tokens.countTokens() != 3) {
                    throw new IOException("Invalid edge: " + line);
                }

                try {
                    graph.addEdge(
                            Integer.parseInt(tokens.nextToken()),
                            Integer.parseInt(tokens.nextToken()),
                            Integer.parseInt(tokens.nextToken())
                    );
                } catch (IllegalArgumentException ex) {
                    throw new IOException("Bad edge: " + line, ex);
                }
            }

            return graph;
        }
    }
}
