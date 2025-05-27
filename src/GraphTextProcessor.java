import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/** 文本处理器类.*/
public class GraphTextProcessor {
  private static final Map<String, Map<String, Integer>> graph = new HashMap<>();

  /** 主函数.*/
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

    // 让用户输入文件路径，到输入正确为止
    String text = null;
    while (text == null) {
      System.out.print("Enter the path of the text file: ");
      String filePath = scanner.nextLine();
      text = readFile(filePath);
      if (text == null) {
        System.out.println("Invalid file path or failed to read file. Please try again.");
      }
    }

    // 处理文本
    text = preprocessText(text);

    // 生成有向图
    buildGraph(text);

    System.out.println("Text processed and graph generated successfully!");


    // 提供功能菜单
    boolean running = true;
    while (running) {
      System.out.println("\nChoose an option:");
      System.out.println("1. Generate and save graph visualization");
      System.out.println("2. Query bridge words");
      System.out.println("3. Generate new text");
      System.out.println("4. Find shortest path between two words");
      System.out.println("5. Calculate PageRank");
      System.out.println("6. Perform random walk");
      System.out.println("7. Exit");

      int choice = scanner.nextInt();
      scanner.nextLine();  // Consume the newline character
      switch (choice) {
        case 1:
          System.out.println("Generating and saving graph visualization...");
          generateGraphVisualization("output_graph.png");
          System.out.println("Graph saved as output_graph.png.");
          break;
        case 2:
          System.out.print("Enter first word: ");
          String word1 = scanner.nextLine();
          System.out.print("Enter second word: ");
          String word2 = scanner.nextLine();
          System.out.println(queryBridgeWords(word1, word2));
          break;
        case 3:
          System.out.print("Enter the text to generate new text: ");
          String inputText = scanner.nextLine();
          System.out.println(generateNewText(inputText));
          break;
        case 4:
          System.out.print("Enter first word: ");
          String startWord = scanner.nextLine();
          System.out.print("Enter second word: ");
          String endWord = scanner.nextLine();
          System.out.println(calcShortestPath(startWord, endWord));
          break;
        case 5:
          Map<String, Double> pageRankResults = calculatePageRank();
          System.out.println("PageRank results:");
          pageRankResults.forEach((word, rank) -> System.out.println(word + ": " + rank));
          break;
        case 6:
          System.out.println(randomWalk());
          break;
        case 7:
          running = false;
          break;
        default:
          System.out.println("Invalid choice. Try again.");
      }
    }

    scanner.close();
  }


  /** 读取文件内容.*/
  public static String readFile(String filePath) {
    StringBuilder content = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(
        new FileInputStream(filePath), StandardCharsets.UTF_8))) {
      String line;
      while ((line = br.readLine()) != null) {
        content.append(line).append(" ");  // 将每一行的内容合并，换行符会被空格替换
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    return content.toString();
  }

  /** 文本预处理.*/
  public static String preprocessText(String text) {
    // 将换行符,回车符替换为空格
    text = text.replaceAll("[\\r\\n]+", " ");

    // 将所有标点符号替换为空格
    text = text.replaceAll("[^a-zA-Z ]", " ");

    // 将多余空格合并为一个空格
    text = text.replaceAll("\\s+", " ").trim();

    // 转换为小写字母
    text = text.toLowerCase();

    return text;
  }

  /** 生成有向图.*/
  public static void buildGraph(String text) {
    String[] words = text.split(" ");
    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];

      // 只考虑非空单词
      if (!word1.isEmpty() && !word2.isEmpty()) {
        graph.putIfAbsent(word1, new HashMap<>());
        graph.get(word1).put(word2, graph.get(word1).getOrDefault(word2, 0) + 1);
      }
    }
  }

  /** 查询桥接词.*/
  public static String queryBridgeWords(String word1, String word2) {
    if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
      return "No " + word1 + " or " + word2 + " in the graph!";
    }

    Set<String> bridgeWords = new HashSet<>();
    for (String intermediateWord : graph.get(word1).keySet()) {
      if (graph.containsKey(intermediateWord) && graph.get(intermediateWord).containsKey(word2)) {
        bridgeWords.add(intermediateWord);
      }
    }

    if (bridgeWords.isEmpty()) {
      return "No bridge words from " + word1 + " to " + word2 + "!";
    } else if (bridgeWords.size() == 1) {
      return "The bridge word from " + word1 + " to " + word2 + " is: "
          + bridgeWords.iterator().next() + ".";
    } else {
      return "The bridge words from " + word1 + " to " + word2 + " are: "
          + String.join(", ", bridgeWords) + ".";
    }
  }


  /** 根据桥接词生成新文本.*/
  public static String generateNewText(String inputText) {
    // 1. 清洗输入文本：非字母替换为空格，小写处理
    String cleanedText = inputText.replaceAll("[^a-zA-Z\\s]", " ").toLowerCase();
    String[] words = cleanedText.trim().split("\\s+");

    if (words.length < 2) {
      return inputText;  // 少于两个单词，无需处理
    }

    StringBuilder result = new StringBuilder();
    Random random = new Random();

    for (int i = 0; i < words.length - 1; i++) {
      String word1 = words[i];
      String word2 = words[i + 1];
      result.append(word1);

      // 查找 bridge words
      Set<String> bridges = new HashSet<>();
      if (graph.containsKey(word1)) {
        for (String mid : graph.get(word1).keySet()) {
          if (graph.containsKey(mid) && graph.get(mid).containsKey(word2)) {
            bridges.add(mid);
          }
        }
      }

      if (!bridges.isEmpty()) {
        List<String> bridgeList = new ArrayList<>(bridges);
        String bridge = bridgeList.get(random.nextInt(bridgeList.size()));
        result.append(" ").append(bridge);
      }

      result.append(" ");
    }

    // 添加最后一个单词
    result.append(words[words.length - 1]);
    return result.toString();
  }

  /** 计算最短路径.*/
  public static String calcShortestPath(String word1, String word2) {
    word1 = word1.toLowerCase();
    if (word2 != null) {
      word2 = word2.toLowerCase();
    }

    if (!graph.containsKey(word1)) {
      return "No such word '" + word1 + "' in the graph!";
    }

    // 初始化
    Map<String, Integer> distances = new HashMap<>();
    Map<String, List<List<String>>> paths = new HashMap<>();
    Set<String> allNodes = new HashSet<>(graph.keySet());
    for (Map<String, Integer> edges : graph.values()) {
      allNodes.addAll(edges.keySet());
    }

    for (String node : allNodes) {
      distances.put(node, Integer.MAX_VALUE);
      paths.put(node, new ArrayList<>());
    }

    distances.put(word1, 0);
    paths.get(word1).add(new ArrayList<>(List.of(word1)));
    PriorityQueue<String> queue = new PriorityQueue<>(Comparator.comparingInt(distances::get));
    queue.add(word1);

    while (!queue.isEmpty()) {
      String current = queue.poll();
      int currDist = distances.get(current);

      if (!graph.containsKey(current)) {
        continue;
      }

      for (Map.Entry<String, Integer> entry : graph.get(current).entrySet()) {
        String neighbor = entry.getKey();
        int weight = entry.getValue();
        int newDist = currDist + weight;

        if (newDist < distances.get(neighbor)) {
          distances.put(neighbor, newDist);
          paths.get(neighbor).clear();
          for (List<String> path : paths.get(current)) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(neighbor);
            paths.get(neighbor).add(newPath);
          }
          queue.add(neighbor);
        } else if (newDist == distances.get(neighbor)) {
          for (List<String> path : paths.get(current)) {
            List<String> newPath = new ArrayList<>(path);
            newPath.add(neighbor);
            paths.get(neighbor).add(newPath);
          }
        }
      }
    }

    StringBuilder sb = new StringBuilder();

    if (word2 == null || word2.isEmpty()) {
      // 模式2：从 word1 到所有其他词的路径
      sb.append("All shortest paths from ").append(word1).append(":\n");
      for (String target : allNodes) {
        if (target.equals(word1)) {
          continue;
        }
        if (distances.get(target) == Integer.MAX_VALUE) {
          sb.append("No path to ").append(target).append(".\n");
        } else {
          sb.append("→ ").append(target).append(" (length = ")
              .append(distances.get(target)).append("):\n");
          List<List<String>> spaths = paths.get(target);
          int idx = 1;
          for (List<String> path : spaths) {
            sb.append("   Path ").append(idx++).append(": ")
                .append(String.join(" -> ", path)).append("\n");
          }
        }
      }
    } else {
      // 模式1：从 word1 到 word2 的所有路径
      if (!distances.containsKey(word2) || distances.get(word2) == Integer.MAX_VALUE) {
        return "No path found from " + word1 + " to " + word2 + ".";
      }
      sb.append("Shortest paths from ").append(word1).append(" to ").append(word2)
          .append(" (length = ").append(distances.get(word2)).append("):\n");
      List<List<String>> spaths = paths.get(word2);
      int idx = 1;
      for (List<String> path : spaths) {
        sb.append("Path ").append(idx++).append(": ").append(String.join(" -> ", path))
            .append("\n");
      }
    }

    return sb.toString();
  }


  /** 计算PageRank.*/
  public static Map<String, Double> calculatePageRank() {
    Map<String, Double> pageRank = new HashMap<>();
    Map<String, Integer> outDegree = new HashMap<>(); // 用来存储每个节点的出度
    Map<String, Integer> wordFrequencies = new HashMap<>(); // 用来存储词频

    // 1. 统计词频
    for (String word : graph.keySet()) {
      wordFrequencies.put(word, wordFrequencies.getOrDefault(word, 0) + 1);
    }

    int totalWords = wordFrequencies.size();
    double totalFrequency = wordFrequencies.values().stream().mapToInt(Integer::intValue).sum();

    // 2. 初始化 PR 值：基础值 + 词频权重
    double totalInitialPr = 0.0;
    for (Map.Entry<String, Integer> entry : wordFrequencies.entrySet()) {
      String word = entry.getKey();
      int frequency = entry.getValue();

      double tf = (double) frequency / totalFrequency;
      pageRank.put(word, 1.0 / totalWords + tf);
      totalInitialPr += pageRank.get(word);
      outDegree.put(word, graph.get(word).size());
    }


    // 3. 归一化初始 PR 值，使其总和为 1
    for (Map.Entry<String, Double> entry : pageRank.entrySet()) {
      double normalized = entry.getValue() / totalInitialPr;
      entry.setValue(normalized);
    }

    double d = 0.85;  // 阻尼系数
    int maxIterations = 100;      // 最大迭代次数
    double tolerance = 0.0001;    // 容忍度
    // 4. PageRank 迭代
    for (int i = 0; i < maxIterations; i++) {
      Map<String, Double> newPageRank = new HashMap<>();
      double maxDiff = 0;
      double danglingPr = 0.0;

      // 计算所有出度为 0 的节点 PR 总和
      for (String node : graph.keySet()) {
        if (outDegree.get(node) == 0) {
          danglingPr += pageRank.get(node);
        }
      }

      // 计算新的 PR 值
      for (String word : graph.keySet()) {
        double rankSum = 0.0;

        for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
          String neighbor = entry.getKey();
          Map<String, Integer> edges = entry.getValue();

          if (edges.containsKey(word)) {
            rankSum += pageRank.get(neighbor) / outDegree.get(neighbor);
          }
        }


        // 处理悬挂节点贡献
        double newRank = (1 - d) / totalWords + d * (rankSum + danglingPr / totalWords);
        newPageRank.put(word, newRank);
        maxDiff = Math.max(maxDiff, Math.abs(newRank - pageRank.get(word)));
      }

      pageRank = newPageRank;

      // 如果最大变化小于容忍度，则停止迭代
      if (maxDiff < tolerance) {
        break;
      }
    }

    // 5. 保留四位小数
    for (Map.Entry<String, Double> entry : pageRank.entrySet()) {
      entry.setValue(Double.parseDouble(String.format("%.4f", entry.getValue())));
    }

    return pageRank;
  }


  /** 随机游走.*/
  public static String randomWalk() {
    if (graph.isEmpty()) {
      return "The graph is empty!";
    }

    Random random = new Random();
    String currentNode = new ArrayList<>(graph.keySet()).get(random.nextInt(graph.size()));
    Set<String> visitedEdges = new HashSet<>();
    StringBuilder walk = new StringBuilder();
    walk.append(currentNode).append(" ");

    while (graph.containsKey(currentNode) && !graph.get(currentNode).isEmpty()) {
      List<String> neighbors = new ArrayList<>(graph.get(currentNode).keySet());
      String nextNode = neighbors.get(random.nextInt(neighbors.size()));
      String edge = currentNode + "->" + nextNode;

      if (visitedEdges.contains(edge)) {
        walk.append(nextNode).append(" "); // 加入重复边目标节点后停止
        break;
      }

      visitedEdges.add(edge);
      currentNode = nextNode;
      walk.append(currentNode).append(" ");
    }

    // 写入磁盘文件
    try (PrintWriter writer = new PrintWriter("random_walk.txt", StandardCharsets.UTF_8)) {
      writer.println(walk.toString().trim());
    } catch (IOException e) {
      return "Error writing random walk to file.";
    }

    return walk.toString().trim();
  }

  /** 生成和保存图形化的有向图.*/
  public static void generateGraphVisualization(String fileName) {
    try {
      StringBuilder dotFileContent = new StringBuilder();
      dotFileContent.append("digraph G {\n");

      // 添加图中的每一条边
      for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
        String node = entry.getKey();
        for (Map.Entry<String, Integer> edge : entry.getValue().entrySet()) {
          String neighbor = edge.getKey();
          dotFileContent.append(String.format("    \"%s\" -> \"%s\" [label=%d];%n", node,
              neighbor, edge.getValue()));
        }
      }

      dotFileContent.append("}\n");

      // 将DOT格式的图保存到临时文件
      File tempFile = File.createTempFile("graph", ".dot");
      try (BufferedWriter writer = new BufferedWriter(
          new FileWriter(tempFile, StandardCharsets.UTF_8))) {
        writer.write(dotFileContent.toString());
      }

      // 使用Graphviz生成图形
      String dotPath = "dot";  // 确保Graphviz已安装
      ProcessBuilder processBuilder = new ProcessBuilder(dotPath, "-Tpng",
          tempFile.getAbsolutePath(), "-o", fileName);
      processBuilder.start().waitFor();

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }
}