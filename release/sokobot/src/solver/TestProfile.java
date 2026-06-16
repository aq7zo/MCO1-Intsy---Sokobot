package solver;

import reader.FileReader;
import reader.MapData;

public class TestProfile {
  public static void main(String[] args) {
    FileReader fileReader = new FileReader();
    MapData mapData = fileReader.readFile("original1");
    if (mapData == null) {
      System.err.println("Failed to read map original2");
      System.exit(1);
    }
    
    int rows = mapData.rows;
    int cols = mapData.columns;
    char[][] map = new char[rows][cols];
    char[][] items = new char[rows][cols];
    
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        switch (mapData.tiles[i][j]) {
          case '#': map[i][j] = '#'; items[i][j] = ' '; break;
          case '@': map[i][j] = ' '; items[i][j] = '@'; break;
          case '$': map[i][j] = ' '; items[i][j] = '$'; break;
          case '.': map[i][j] = '.'; items[i][j] = ' '; break;
          case '+': map[i][j] = '.'; items[i][j] = '@'; break;
          case '*': map[i][j] = '.'; items[i][j] = '$'; break;
          case ' ': map[i][j] = ' '; items[i][j] = ' '; break;
        }
      }
    }
    
    System.out.println("Running solver on original2...");
    SokoBot bot = new SokoBot();
    long start = System.nanoTime();
    String solution = bot.solveSokobanPuzzle(cols, rows, map, items);
    long end = System.nanoTime();
    
    System.out.println("Result solution length: " + solution.length());
    System.out.println("Solution: " + solution);
    System.out.println("Time taken: " + (end - start)/1e9 + "s");
  }
}
