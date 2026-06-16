# SokoBot: Specification-Aligned Architecture Checklist

This architecture checklist reflects the official MCO1 SokoBot specification in `release/specifications.pdf`.

## 1. Project Boundary
- [x] **Language and Runtime**: Java with `javac` and `java` available from the command line.
- [x] **Starter Program Ownership**:
    - [x] `main` package: provided execution logic and mode switching.
    - [x] `gui` package: provided rendering, animation, and 15-second timeout behavior.
    - [x] `reader` package: provided map loading and text-file parsing.
    - [x] `maps/`: provided sample levels.
- [x] **Allowed Code Area**:
    - [x] Modify or add code only inside the `solver` package.
    - [x] Main entry point remains `SokoBot.solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData)`.
- [x] **Library Boundary**:
    - [x] Standard Java collections and data structures are allowed.
    - [x] External data-structure libraries are allowed if needed.
    - [x] External libraries that perform AI/search algorithms are not allowed.

## 2. Input and Output Contract
- [x] **Input Signature**:
    - [x] `public String solveSokobanPuzzle(int width, int height, char[][] mapData, char[][] itemsData)`.
- [x] **Map Representation**:
    - [x] `mapData` contains static elements: wall `#`, target `.`, or space.
    - [x] `itemsData` contains movable elements: player `@`, crate `$`, or space.
    - [x] Player and crates may stand on target cells because targets remain in `mapData`.
- [x] **Output Format**:
    - [x] Return one string containing the bot's moves in order.
    - [x] Use only `u`, `d`, `l`, and `r`.
    - [x] The returned string must be playable by the provided GUI.

## 3. Core State Model
- [x] **State Representation**:
    - [x] Store player position `(row, col)`.
    - [x] Store crate positions in a hashable structure.
    - [x] Keep static map data separate from movable item state.
    - [x] Implement `equals()` and `hashCode()` using player and crate positions.
- [x] **Sokoban Rule Enforcement**:
    - [x] Allow player movement in four orthogonal directions.
    - [x] Prevent walking through walls.
    - [x] Allow a single-crate push only when the cell beyond the crate is empty or a target without another crate.
    - [x] Prevent pushing multiple crates.
    - [x] Prevent pulling crates.
    - [x] Detect success when every crate is on a target.

## 4. Solver Architecture
- [x] **Algorithm Responsibility**:
    - [x] Select, implement, and justify the search algorithm; the specification leaves this design choice to the team.
    - [x] Avoid naive-only search if it cannot meet the 15-second and 80% performance expectations.
- [x] **Recommended Direction**:
    - [x] Use push-based informed search, such as A* or weighted A*, as the primary solver.
    - [x] Use player-reachability BFS inside each state to determine which crate pushes are currently possible.
    - [x] Store enough path information to return the exact move string, not only the crate-push sequence.
- [x] **Frontier and Repetition Control**:
    - [x] Use a priority queue or other frontier appropriate to the chosen algorithm.
    - [x] Use a visited or best-cost table keyed by crate positions and relevant player position.
    - [x] Avoid expanding equivalent or worse repeated states.
- [x] **Heuristic and Pruning**:
    - [x] Estimate remaining cost using crate-to-target distances.
    - [x] Prefer assignment-aware crate-to-target matching if feasible.
    - [x] Prune obvious deadlocks such as crates stuck in non-goal corners.
    - [x] Add stronger dead-square or freeze-deadlock detection if needed for performance.
- [x] **Time and Memory Limits**:
    - [x] Self-stop before the GUI's 15-second timeout, preferably around 14.5 seconds.
    - [x] Return promptly with a found valid solution.
    - [x] Keep memory usage practical for hidden tests with around 2 to 8 crates.

## 5. Evaluation Architecture
- [ ] **Automated/Repeatable Testing**:
    - [ ] Test every provided map.
    - [ ] Test additional easy-to-medium Sokoban maps.
    - [ ] Record solve success, solve time, and move count.
- [ ] **Performance Target**:
    - [ ] Aim to solve at least 80% of hidden grading levels.
    - [ ] Analyze which puzzle patterns the bot handles well or poorly.

## 6. Report and Deliverables
- [ ] **Report Content**:
    - [ ] Describe the implemented algorithm in project-specific terms.
    - [ ] Explain state representation, action generation, and search rationale.
    - [ ] Present evaluation results, strengths, weaknesses, and examples.
    - [ ] Discuss challenges in automated Sokoban solving.
    - [ ] Include a table of contributions.
- [ ] **Report Restrictions**:
    - [ ] Keep the report within four A4 pages unless instructor permission is granted.
    - [ ] Do not use generative AI to write or improve the report text.
- [ ] **Submission Artifacts**:
    - [ ] Submit the full project directory as a zip file.
    - [ ] Submit the report as a PDF.
