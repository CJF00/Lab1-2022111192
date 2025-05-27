import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

public class WhiteBoxTest {

  @BeforeClass
  public static void setup() {
    String filePath = Paths.get("test.txt").toAbsolutePath().toString();
    String content = GraphTextProcessor.readFile(filePath);
    if (content == null) {
      throw new RuntimeException("Failed to read test file: " + filePath);
    }
    String preprocessed = GraphTextProcessor.preprocessText(content);
    GraphTextProcessor.buildGraph(preprocessed);
  }

  //单词不存在
  @Test
  public void testStartWordNotExist() {
    String word1 = "unknown";
    String word2 = "work";
    String result = GraphTextProcessor.calcShortestPath(word1, word2);
    assertEquals("No such word 'unknown' in the graph!", result);
    System.out.println("测试1通过");
  }

  //终点为空
  @Test
  public void testNullOrEmptyEndWordAllPaths() {
    String word1 = "you";
    String word2 = "";

    String resultNull = GraphTextProcessor.calcShortestPath(word1, word2);
    assertTrue(resultNull.startsWith("All shortest paths from you:"));
    assertTrue(resultNull.contains("Path 1: you -> work -> or"));
    System.out.println("测试2通过");
  }

  //两词间有路径
  @Test
  public void testValidShortestPath() {
    String word1 = "are";
    String word2 = "student";
    String result = GraphTextProcessor.calcShortestPath(word1, word2);
    assertTrue(result.startsWith("Shortest paths from are to student (length = 3):"));
    System.out.println("测试3通过");
  }

  //两词间无路径
  @Test
  public void testEndWordNoPath() {
    String word1 = "a";
    String word2 = "are";
    String result = GraphTextProcessor.calcShortestPath(word1, word2);
    assertEquals("No path found from a to are.", result);
    System.out.println("测试4通过");
  }
}