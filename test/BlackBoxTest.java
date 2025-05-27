import org.junit.BeforeClass;
import org.junit.Test;
import java.nio.file.Paths;
import static org.junit.Assert.*;
public class BlackBoxTest {

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
  // EC1: 输入少于两个单词
  @Test
  public void testEC1_TooShortInput() {
    String word1 = "are";
    String word2 = ""; // 空字符串
    String output = GraphTextProcessor.queryBridgeWords(word1, word2);
    assertEquals("No are or  in the graph!", output);
    System.out.println("测试1通过");
  }

  // EC2: 存在桥接词
  @Test
  public void testEC2_BridgeExists() {
    String word1 = "do";
    String word2 = "work";
    String output = GraphTextProcessor.queryBridgeWords(word1, word2);
    assertEquals("The bridge word from do to work is: you.",output);
    System.out.println("测试2通过");
  }

  // EC3: 所有单词对无桥接词
  @Test
  public void testEC3_NoBridge() {
    String word1 = "do";
    String word2 = "are";
    String output = GraphTextProcessor.queryBridgeWords(word1, word2);
    assertEquals("No bridge words from do to are!", output);
    System.out.println("测试3通过");
  }

  // EC4: 输入单词不在图中
  @Test
  public void testEC4_UnknownWords() {
    String word1 = "yes";
    String word2 = "am";
    String output = GraphTextProcessor.queryBridgeWords(word1, word2);
    assertEquals("No yes or am in the graph!", output);
    System.out.println("测试4通过");
  }
}
