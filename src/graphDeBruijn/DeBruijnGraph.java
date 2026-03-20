package graphDeBruijn;

import java.util.*;

public class DeBruijnGraph {

    // Класс ребра графа
    public static class Edge {
        public final String from;   // начальная вершина (k-мер)
        public final String to;     // конечная вершина (k-мер)
        public final String label;  // само ребро = (k+1)-мер
        public int count;           // покрытие 

        public Edge(String from, String to, String label) {
            this.from = from;
            this.to = to;
            this.label = label;
            this.count = 1; // при создании ребро встречено 1 раз
        }
    }

    private final int k; // k-мера 

    // исходящие рёбра
    private final Map<String, Map<String, Edge>> outEdges = new HashMap<>();
    // входящие рёбра
    private final Map<String, Map<String, Edge>> inEdges = new HashMap<>();

    public DeBruijnGraph(int k) {
        this.k = k;
    }

    public int getK() {
        return k;
    }

    // функция для обработки последовательности и добавлении в граф
    public void processSequence(String seq) {
        if (seq.length() < k + 1)
            return;

        for (int i = 0; i <= seq.length() - (k + 1); i++) {

            //  берем (k+1) мера - ребро
            String k1 = seq.substring(i, i + k + 1);
            // выделяем вершины (k меры)
            String from = k1.substring(0, k);
            String to = k1.substring(1);

            // добавляем ребро в граф
            addEdge(from, to, k1);
        }
    }

    // Добавление ребра
    private void addEdge(String from, String to, String label) {

        // получаем (или создаём) список исходящих рёбер для вершины from
        Map<String, Edge> outs = outEdges.computeIfAbsent(from, k -> new HashMap<>());

        // проверяем, есть ли уже ребро from -> to
        Edge e = outs.get(to);

        // если нет — создаём новое ребро и добавляем в исходящие рёбра
        if (e == null) {
            e = new Edge(from, to, label);
            outs.put(to, e);

            // добавляем в множества
            inEdges.computeIfAbsent(to, k -> new HashMap<>()).put(from, e);
            outEdges.computeIfAbsent(to, k -> new HashMap<>());
            inEdges.computeIfAbsent(from, k -> new HashMap<>());
        } else {
            // если ребро уже есть — увеличиваем покрытие
            e.count++;
        }
    }

    // Возвращает все вершины графа
    public Collection<String> getNodes() {
        Set<String> nodes = new HashSet<>();
        nodes.addAll(outEdges.keySet());
        nodes.addAll(inEdges.keySet());

        return nodes;
    }

    // Возвращает все рёбра графа
    public Collection<Edge> getEdges() {
        List<Edge> list = new ArrayList<>();
        for (Map<String, Edge> m : outEdges.values())
            list.addAll(m.values());

        return list;
    }

    // Возвращает исходящие рёбра вершины
    public Map<String, Edge> getOutEdges(String node) {
        return outEdges.getOrDefault(node, Collections.emptyMap());
    }

    // Степень выхода
    public int outDegree(String node) {
        return outEdges.getOrDefault(node, Collections.emptyMap()).size();
    }

    // Возвращает входящие рёбра вершины
    public Map<String, Edge> getInEdges(String node) {
        return inEdges.getOrDefault(node, Collections.emptyMap());
    }

    // Степень входа
    public int inDegree(String node) {
        return inEdges.getOrDefault(node, Collections.emptyMap()).size();
    }

    // Удаление рёбер с низким покрытием
    public void removeLowCoverageEdges(int minCoverage) {

        List<Edge> toRemove = new ArrayList<>();

        // ищем рёбра с покрытием ниже порога
        for (Edge e : getEdges()) {
            if (e.count < minCoverage)
                toRemove.add(e);
        }

        // удаляем найденные рёбра
        for (Edge e : toRemove)
            removeEdge(e);

        cleanupIsolatedNodes();
    }

    // Удаление коротких тупиковых ветвей (tips)
    public void removeTips() {
        boolean changed;

        do {
            changed = false;

            List<String> nodes = new ArrayList<>(getNodes());

            for (String node : nodes) {

                // вершина начало тупика
                if (inDegree(node) == 0 && outDegree(node) > 0) {
                    // ищем путь вперёд
                    List<Edge> path = collectTipPath(node, 2 * k);

                    if (path != null) {
                        // удаляем весь путь
                        for (Edge e : path)
                            removeEdge(e);
                        changed = true;
                    }
                }
            }
            if (changed)
                cleanupIsolatedNodes();

        } while (changed); // повторяем, пока есть изменения
    }

    // Собирает путь тупика
    private List<Edge> collectTipPath(String start, int maxLen) {

        List<Edge> path = new ArrayList<>();
        String curr = start;
        int len = 0;

        while (true) {
            // если нет исходящих — конец
            if (outDegree(curr) == 0)
                break;

            // стоп если это не старт и есть ветвление
            if (curr != start && (inDegree(curr) != 1 || outDegree(curr) != 1))
                break;

            // берём исходящее ребро
            Map<String, Edge> outs = getOutEdges(curr);
            Edge e;
            if (outs.size() != 1) // должно быть единственным
                e = null;
            else
                e = outs.values().iterator().next();

            if (e == null) // не нашли значит стоп
                break;

            path.add(e);
            len++;

            // ищем дальше
            curr = e.to;

            // не ищем слишком длинные пути
            if (len > maxLen)
                return null;

            // останавливаемся в ветвлении
            if (inDegree(curr) > 1 || outDegree(curr) > 1)
                break;
        }

        // возвращаем путь, если он короткий
        return path;
    }

    // Удаление ребра
    private void removeEdge(Edge e) {
        // удаляем из исходящих
        Map<String, Edge> outs = outEdges.get(e.from);
        if (outs != null)
            outs.remove(e.to);

        // удаляем из входящих
        Map<String, Edge> ins = inEdges.get(e.to);
        if (ins != null)
            ins.remove(e.from);
    }

    // Удаление изолированных вершин
    private void cleanupIsolatedNodes() {

        List<String> nodes = new ArrayList<>(getNodes());

        for (String n : nodes) {
            // если нет ни входящих, ни исходящих рёбер
            if (inDegree(n) == 0 && outDegree(n) == 0) {
                outEdges.remove(n);
                inEdges.remove(n);
            }
        }
    }
}