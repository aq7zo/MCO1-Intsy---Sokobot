package solver;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class SokoBot {
  private static final long TIME_LIMIT_NANOS = 14_500_000_000L;
  private static final int INF = 1_000_000_000;
  private static final int[] DR = {-1, 1, 0, 0};
  private static final int[] DC = {0, 0, -1, 1};
  private static final char[] MOVE = {'u', 'd', 'l', 'r'};

  private int width;
  private int height;
  private char[][] mapData;
  private boolean[] targets;
  private boolean[] deadSquares;
  private int[] targetCells;
  private int[][] targetDistances;
  private long deadline;
  private java.util.ArrayList<Integer>[] masksByBitCount;

  public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData) {
    this.width = width;
    this.height = height;
    this.mapData = mapData;
    this.deadline = System.nanoTime() + TIME_LIMIT_NANOS;

    int player = -1;
    int[] crates = new int[width * height];
    int crateCount = 0;
    Set<Integer> targetSet = new HashSet<Integer>();

    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        int cell = encode(r, c);
        if (mapData[r][c] == '.') {
          targetSet.add(cell);
        }
        if (itemsData[r][c] == '@') {
          player = cell;
        } else if (itemsData[r][c] == '$') {
          crates[crateCount++] = cell;
        }
      }
    }

    if (player < 0 || crateCount == 0) {
      return "";
    }

    crates = Arrays.copyOf(crates, crateCount);
    Arrays.sort(crates);
    initializeTargets(targetSet);

    if (isSolved(crates)) {
      return "";
    }

    PriorityQueue<Node> frontier = new PriorityQueue<Node>();
    HashMap<String, Integer> bestCost = new HashMap<String, Integer>();

    Node start = new Node(player, crates, "", 0, heuristic(crates));
    frontier.add(start);


    int poppedNodes = 0;
    long startTime = System.nanoTime();
    while (!frontier.isEmpty() && !timedOut()) {
      Node current = frontier.poll();
      poppedNodes++;
      if (poppedNodes % 1000 == 0) {
        System.out.println("Popped: " + poppedNodes + ", frontier size: " + frontier.size() + ", bestCost size: " + bestCost.size() + ", elapsed: " + (System.nanoTime() - startTime)/1e9 + "s");
      }
      String currentKey = key(current.player, current.crates);
      Integer knownCost = bestCost.get(currentKey);
      if (knownCost != null && current.pushes > knownCost) {
        continue;
      }

      if (isSolved(current.crates)) {
        return current.moves;
      }

      Reachability reachability = computeReachability(current.player, current.crates);
      int repPlayer = getRepresentativePlayer(current.player, reachability.reachable);
      String repKey = key(repPlayer, current.crates);
      Integer repKnownCost = bestCost.get(repKey);
      if (repKnownCost != null && current.pushes >= repKnownCost) {
        continue;
      }

      boolean[] crateAt = crateMap(current.crates);
      if (false && hasComponentDeadlock(reachability, crateAt)) {
        continue;
      }
      bestCost.put(repKey, current.pushes);

      for (int i = 0; i < current.crates.length; i++) {
        int crate = current.crates[i];
        int crateRow = row(crate);
        int crateCol = col(crate);

        for (int d = 0; d < 4; d++) {
          int pushRow = crateRow - DR[d];
          int pushCol = crateCol - DC[d];
          int nextRow = crateRow + DR[d];
          int nextCol = crateCol + DC[d];

          if (!isFloor(pushRow, pushCol) || !isFloor(nextRow, nextCol)) {
            continue;
          }

          int pushCell = encode(pushRow, pushCol);
          int nextCrateCell = encode(nextRow, nextCol);

          if (!reachability.reachable[pushCell] || crateAt[nextCrateCell]) {
            continue;
          }
          if (false && isDeadSquare(nextCrateCell)) {
            continue;
          }

          int[] nextCrates = current.crates.clone();
          nextCrates[i] = nextCrateCell;
          Arrays.sort(nextCrates);

          boolean[] nextCrateAt = crateMap(nextCrates);
          if (false && has2x2Deadlock(nextCrates, nextCrateAt)) {
            continue;
          }

          String nextMoves = current.moves + reachability.paths[pushCell] + MOVE[d];
          int nextPlayer = crate;
          String nextKey = key(nextPlayer, nextCrates);
          Integer best = bestCost.get(nextKey);

          int nextPushes = current.pushes + 1;
          if (best == null || nextPushes < best) {
            int h = heuristic(nextCrates);
            if (h >= INF / 2) {
              continue;
            }

            bestCost.put(nextKey, nextPushes);
            frontier.add(new Node(nextPlayer, nextCrates, nextMoves, nextPushes, h));
          }
        }
      }
    }

    System.out.println("Search finished. Popped nodes: " + poppedNodes + ", frontier empty: " + frontier.isEmpty() + ", timed out: " + timedOut());
    return "";
  }

  private void initializeTargets(Set<Integer> targetSet) {
    targets = new boolean[width * height];
    targetCells = new int[targetSet.size()];

    int index = 0;
    for (Integer target : targetSet) {
      targets[target] = true;
      targetCells[index++] = target;
    }

    targetDistances = new int[targetCells.length][width * height];
    for (int i = 0; i < targetCells.length; i++) {
      targetDistances[i] = computeCellDistances(targetCells[i]);
    }

    deadSquares = computeDeadSquares();

    masksByBitCount = new java.util.ArrayList[targetCells.length + 1];
    for (int i = 0; i <= targetCells.length; i++) {
      masksByBitCount[i] = new java.util.ArrayList<Integer>();
    }
    for (int mask = 0; mask < (1 << targetCells.length); mask++) {
      int count = Integer.bitCount(mask);
      if (count <= targetCells.length) {
        masksByBitCount[count].add(mask);
      }
    }
  }

  private int[] computeCellDistances(int start) {
    int[] distances = new int[width * height];
    Arrays.fill(distances, INF);

    ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
    distances[start] = 0;
    queue.add(start);

    while (!queue.isEmpty()) {
      int cell = queue.remove();
      int r = row(cell);
      int c = col(cell);

      for (int d = 0; d < 4; d++) {
        int nr = r + DR[d];
        int nc = c + DC[d];
        if (!isFloor(nr, nc)) {
          continue;
        }

        int next = encode(nr, nc);
        if (distances[next] == INF) {
          distances[next] = distances[cell] + 1;
          queue.add(next);
        }
      }
    }

    return distances;
  }

  private boolean[] computeDeadSquares() {
    boolean[] live = new boolean[width * height];
    ArrayDeque<Integer> queue = new ArrayDeque<Integer>();

    for (int target : targetCells) {
      live[target] = true;
      queue.add(target);
    }

    while (!queue.isEmpty()) {
      int cell = queue.remove();
      int r = row(cell);
      int c = col(cell);

      for (int d = 0; d < 4; d++) {
        int previousRow = r - DR[d];
        int previousCol = c - DC[d];
        int playerStandRow = previousRow - DR[d];
        int playerStandCol = previousCol - DC[d];

        if (!isFloor(previousRow, previousCol) || !isFloor(playerStandRow, playerStandCol)) {
          continue;
        }

        int previous = encode(previousRow, previousCol);
        if (!live[previous]) {
          live[previous] = true;
          queue.add(previous);
        }
      }
    }

    boolean[] dead = new boolean[width * height];
    for (int r = 0; r < height; r++) {
      for (int c = 0; c < width; c++) {
        int cell = encode(r, c);
        dead[cell] = isFloor(r, c) && !live[cell] && !targets[cell];
      }
    }

    return dead;
  }

  private Reachability computeReachability(int player, int[] crates) {
    boolean[] crateAt = crateMap(crates);
    boolean[] reachable = new boolean[width * height];
    String[] paths = new String[width * height];
    ArrayDeque<Integer> queue = new ArrayDeque<Integer>();

    reachable[player] = true;
    paths[player] = "";
    queue.add(player);

    while (!queue.isEmpty()) {
      int cell = queue.remove();
      int r = row(cell);
      int c = col(cell);

      for (int d = 0; d < 4; d++) {
        int nr = r + DR[d];
        int nc = c + DC[d];
        if (!isFloor(nr, nc)) {
          continue;
        }

        int next = encode(nr, nc);
        if (reachable[next] || crateAt[next]) {
          continue;
        }

        reachable[next] = true;
        paths[next] = paths[cell] + MOVE[d];
        queue.add(next);
      }
    }

    return new Reachability(reachable, paths);
  }
  private int heuristic(int[] crates) {
    int crateCount = crates.length;
    int targetCount = targetCells.length;
    if (crateCount > targetCount) {
      return INF;
    }

    int[] dp = new int[1 << targetCount];
    Arrays.fill(dp, INF);
    dp[0] = 0;

    for (int i = 0; i < crateCount; i++) {
      int[] nextDp = new int[1 << targetCount];
      Arrays.fill(nextDp, INF);

      java.util.ArrayList<Integer> activeMasks = masksByBitCount[i];
      for (int m = 0; m < activeMasks.size(); m++) {
        int mask = activeMasks.get(m);
        if (dp[mask] >= INF) {
          continue;
        }

        for (int t = 0; t < targetCount; t++) {
          if ((mask & (1 << t)) != 0) {
            continue;
          }

          int distance = targetDistances[t][crates[i]];
          if (distance >= INF) {
            continue;
          }

          int nextMask = mask | (1 << t);
          nextDp[nextMask] = Math.min(nextDp[nextMask], dp[mask] + distance);
        }
      }

      dp = nextDp;
    }

    int best = INF;
    for (int value : dp) {
      best = Math.min(best, value);
    }

    return best;
  }
  private boolean isSolved(int[] crates) {
    for (int crate : crates) {
      if (!targets[crate]) {
        return false;
      }
    }
    return true;
  }

  private int getRepresentativePlayer(int player, boolean[] reachable) {
    for (int i = 0; i < reachable.length; i++) {
      if (reachable[i]) {
        return i;
      }
    }
    return player;
  }

  private boolean has2x2Deadlock(int[] crates, boolean[] crateAt) {
    for (int crate : crates) {
      int r = row(crate);
      int c = col(crate);

      if (is2x2DeadlockBlock(r, c, r, c + 1, r + 1, c, r + 1, c + 1, crateAt)) return true;
      if (is2x2DeadlockBlock(r - 1, c, r - 1, c + 1, r, c, r, c + 1, crateAt)) return true;
      if (is2x2DeadlockBlock(r, c - 1, r, c, r + 1, c - 1, r + 1, c, crateAt)) return true;
      if (is2x2DeadlockBlock(r - 1, c - 1, r - 1, c, r, c - 1, r, c, crateAt)) return true;
    }
    return false;
  }

  private boolean is2x2DeadlockBlock(int r1, int c1, int r2, int c2, int r3, int c3, int r4, int c4, boolean[] crateAt) {
    if (r1 < 0 || r1 >= height || c1 < 0 || c1 >= width) return false;
    if (r2 < 0 || r2 >= height || c2 < 0 || c2 >= width) return false;
    if (r3 < 0 || r3 >= height || c3 < 0 || c3 >= width) return false;
    if (r4 < 0 || r4 >= height || c4 < 0 || c4 >= width) return false;

    if (!isWallOrCrate(r1, c1, crateAt)) return false;
    if (!isWallOrCrate(r2, c2, crateAt)) return false;
    if (!isWallOrCrate(r3, c3, crateAt)) return false;
    if (!isWallOrCrate(r4, c4, crateAt)) return false;

    return isCrateNotOnTarget(r1, c1, crateAt) ||
           isCrateNotOnTarget(r2, c2, crateAt) ||
           isCrateNotOnTarget(r3, c3, crateAt) ||
           isCrateNotOnTarget(r4, c4, crateAt);
  }

  private boolean isWallOrCrate(int r, int c, boolean[] crateAt) {
    if (mapData[r][c] == '#') return true;
    int cell = encode(r, c);
    return crateAt[cell];
  }

  private boolean isCrateNotOnTarget(int r, int c, boolean[] crateAt) {
    int cell = encode(r, c);
    return crateAt[cell] && !targets[cell];
  }

  private boolean hasComponentDeadlock(Reachability reachability, boolean[] crateAt) {
    int unoccupiedCount = 0;
    ArrayDeque<Integer> queue = new ArrayDeque<Integer>();
    boolean[] inS = new boolean[width * height];
    
    for (int target : targetCells) {
      if (!crateAt[target]) {
        unoccupiedCount++;
        inS[target] = true;
        queue.add(target);
      }
    }
    
    if (unoccupiedCount == 0) {
      return false; // No unoccupied targets
    }
    
    while (!queue.isEmpty()) {
      int cell = queue.remove();
      int r = row(cell);
      int c = col(cell);
      
      for (int d = 0; d < 4; d++) {
        int nr = r + DR[d];
        int nc = c + DC[d];
        if (!isFloor(nr, nc)) {
          continue;
        }
        int neighbor = encode(nr, nc);
        if (!inS[neighbor] && !crateAt[neighbor]) {
          inS[neighbor] = true;
          queue.add(neighbor);
        }
      }
    }
    
    for (int cell = 0; cell < width * height; cell++) {
      if (inS[cell] && reachability.reachable[cell]) {
        return false;
      }
    }
    
    for (int cell = 0; cell < width * height; cell++) {
      if (!inS[cell]) {
        continue;
      }
      int r = row(cell);
      int c = col(cell);
      
      for (int d = 0; d < 4; d++) {
        int vr = r + DR[d];
        int vc = c + DC[d];
        int wr = r + 2 * DR[d];
        int wc = c + 2 * DC[d];
        
        if (!isFloor(vr, vc) || !isFloor(wr, wc)) {
          continue;
        }
        
        int v = encode(vr, vc);
        int w = encode(wr, wc);
        
        if (crateAt[v] && reachability.reachable[w]) {
          return false;
        }
      }
    }
    
    return true;
  }

  private boolean isDeadSquare(int cell) {
    return deadSquares[cell] && !targets[cell];
  }

  private boolean[] crateMap(int[] crates) {
    boolean[] crateAt = new boolean[width * height];
    for (int crate : crates) {
      crateAt[crate] = true;
    }
    return crateAt;
  }

  private String key(int player, int[] crates) {
    StringBuilder builder = new StringBuilder();
    builder.append(player).append(':');
    for (int crate : crates) {
      builder.append(crate).append(',');
    }
    return builder.toString();
  }

  private boolean timedOut() {
    return System.nanoTime() >= deadline;
  }

  private boolean isFloor(int r, int c) {
    return r >= 0 && r < height && c >= 0 && c < width && mapData[r][c] != '#';
  }

  private int encode(int r, int c) {
    return r * width + c;
  }

  private int row(int cell) {
    return cell / width;
  }

  private int col(int cell) {
    return cell % width;
  }

  private static class Reachability {
    boolean[] reachable;
    String[] paths;

    Reachability(boolean[] reachable, String[] paths) {
      this.reachable = reachable;
      this.paths = paths;
    }
  }

  private static class Node implements Comparable<Node> {
    int player;
    int[] crates;
    String moves;
    int pushes;
    int heuristic;
    int priority;

    Node(int player, int[] crates, String moves, int pushes, int heuristic) {
      this.player = player;
      this.crates = crates;
      this.moves = moves;
      this.pushes = pushes;
      this.heuristic = heuristic;
      this.priority = pushes + heuristic * 3;
    }

    @Override
    public int compareTo(Node other) {
      if (priority != other.priority) {
        return priority - other.priority;
      }
      if (heuristic != other.heuristic) {
        return heuristic - other.heuristic;
      }
      return moves.length() - other.moves.length();
    }
  }
}
