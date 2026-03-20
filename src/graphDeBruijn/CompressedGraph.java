package graphDeBruijn;

import java.util.*;

public class CompressedGraph {

        // Сжатое ребро хранит путь как одну последовательность
        public record CEdge(String from, String to, String seq, int kmerCount, int covSum) {

        // среднее покрытие
            public double avgCoverage() {
                if (kmerCount == 0) return 0.0;
                return (double) covSum / kmerCount;
            }
        }

    private final int k; // k мера
    private final List<CEdge> edges; // список сжатых рёбер

    public CompressedGraph(int k, List<CEdge> edges) {
        this.k = k;
        this.edges = edges;
    }

    public int getK() {
        return k;
    }

    public List<CEdge> edges() {
        return edges;
    }

    // Основной метод сжатия графа
    public static CompressedGraph compress(DeBruijnGraph graph) {
        int k = graph.getK();
        List<CEdge> result = new ArrayList<>();
        Set<DeBruijnGraph.Edge> visited = new HashSet<>(); // чтобы не проходить ребро дважды

        // Строим пути от ветвящихся вершин
        for (String node : graph.getNodes()) {
            boolean isBranch = graph.inDegree(node) != 1 || graph.outDegree(node) != 1;
            if (!isBranch)
                continue;

            for (DeBruijnGraph.Edge e : graph.getOutEdges(node).values()) {
                if (visited.contains(e))
                    continue;

                PathBuild pb = buildPath(graph, e, visited);
                result.add(new CEdge(node, pb.end, pb.seq, pb.kmerCount, pb.covSum));
            }
        }

        // Обрабатываем циклы
        for (String node : graph.getNodes()) {
            if (graph.inDegree(node) == 1 && graph.outDegree(node) == 1) {
                DeBruijnGraph.Edge e = graph.getOutEdges(node).values().iterator().next();
                if (visited.contains(e)) continue;

                PathBuild pb = buildCycle(graph, e, visited);
                result.add(new CEdge(node, pb.end, pb.seq, pb.kmerCount, pb.covSum));
            }
        }

        return new CompressedGraph(k, result);
    }

    // Хранение результата построения пути
    private static class PathBuild {
        String end;      // конечная вершина
        String seq;      // последовательность
        int kmerCount;   // количество рёбер
        int covSum;      // сумма покрытий
    }

    // Строит простой путь
    private static PathBuild buildPath(DeBruijnGraph g, DeBruijnGraph.Edge start,
                                       Set<DeBruijnGraph.Edge> visited) {

        StringBuilder seq = new StringBuilder(start.label); // начинаем с первого k-мера
        int kmerCount = 1;
        int covSum = start.count;

        visited.add(start);
        String curr = start.to;

        // идём по линейной цепочке
        while (g.inDegree(curr) == 1 && g.outDegree(curr) == 1) {
            DeBruijnGraph.Edge next = g.getOutEdges(curr).values().iterator().next();
            if (visited.contains(next))
                break;

            // добавляем только последний символ
            seq.append(next.label.charAt(next.label.length() - 1));
            kmerCount++;
            covSum += next.count;

            visited.add(next);
            curr = next.to;
        }

        PathBuild pb = new PathBuild();
        pb.end = curr;
        pb.seq = seq.toString();
        pb.kmerCount = kmerCount;
        pb.covSum = covSum;
        return pb;
    }

    // Построение цикла
    private static PathBuild buildCycle(DeBruijnGraph g, DeBruijnGraph.Edge start,
                                        Set<DeBruijnGraph.Edge> visited) {

        StringBuilder seq = new StringBuilder(start.label);
        int kmerCount = 1;
        int covSum = start.count;

        visited.add(start);
        String curr = start.to;

        // идём по циклу, пока не вернёмся в посещённое ребро
        while (true) {
            DeBruijnGraph.Edge next = g.getOutEdges(curr).values().iterator().next();
            if (visited.contains(next)) break;

            seq.append(next.label.charAt(next.label.length() - 1));
            kmerCount++;
            covSum += next.count;

            visited.add(next);
            curr = next.to;
        }

        PathBuild pb = new PathBuild();
        pb.end = curr;
        pb.seq = seq.toString();
        pb.kmerCount = kmerCount;
        pb.covSum = covSum;
        return pb;
    }
}