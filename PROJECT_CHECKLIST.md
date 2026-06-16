# SokoBot: Specification-Aligned Project Checklist

Based on `release/specifications.pdf`, this checklist tracks the required work for MCO1 SokoBot.

## Phase 1: Specification Review & Environment Setup
- [x] **Read the Official Specification**:
    - [x] Confirm the project goal: design and implement an AI algorithm that solves Sokoban puzzles.
    - [x] Confirm the bot must model Sokoban as a state-based problem.
    - [x] Confirm the target is easy Sokoban puzzles, with hidden grading levels expected to have around 2 to 8 crates.
- [x] **Run the Starter Program**:
    - [x] Run `freeplay.bat` / `freeplay.sh` to confirm JDK installation and gameplay behavior.
    - [x] Run `sokobot.bat` / `sokobot.sh` to observe the baseline bot behavior.
- [x] **Understand Input and Output**:
    - [x] Inspect `SokoBot.java` and the `solveSokobanPuzzle` method.
    - [x] Map `mapData` as static tiles: wall `#`, target `.`, otherwise space.
    - [x] Map `itemsData` as movable objects: player `@`, crate `$`, otherwise space.
    - [x] Confirm the output is a move string using only `u`, `d`, `l`, and `r`.
- [x] **Constraint Awareness**:
    - [x] Confirm the GUI imposes a 15-second thinking time limit.
    - [x] Confirm implementation changes are allowed only inside the `solver` package.
    - [x] Confirm data-structure libraries are allowed, but AI/search algorithm libraries are not allowed.
    - [x] Confirm the report must not be written or improved using generative AI.

## Phase 2: Core Logic & Knowledge Representation
- [x] **State Representation**:
    - [x] Store player position `(row, col)`.
    - [x] Store crate positions in a hashable structure.
    - [x] Use `mapData` for static validation against walls and targets.
    - [x] Implement `equals()` and `hashCode()` for state identity.
- [x] **Sokoban Rules and Transitions**:
    - [x] Generate legal actions in the four orthogonal directions.
    - [x] Prevent walking through walls.
    - [x] Allow pushing exactly one crate only when the destination behind it is free.
    - [x] Prevent pushing crates through walls or into other crates.
    - [x] Prevent pulling crates.
    - [x] Check the goal condition: all crates are on target spaces.

## Phase 3: SokoBot Algorithm & Optimization
- [x] **Algorithm Selection**:
    - [x] Choose and justify the search strategy; the specification does not prescribe one fixed algorithm.
    - [x] Prefer an informed solver, such as push-based A* or weighted A*, over naive move-by-move BFS.
    - [x] Keep all implementation code inside the `solver` package.
- [x] **Search Implementation**:
    - [x] Parse the initial player, crates, and targets from `itemsData` and `mapData`.
    - [x] Use a frontier such as a priority queue for informed search.
    - [x] Use a visited or best-cost structure to prevent redundant exploration.
    - [x] Return the full playable move string, including player walking moves and crate pushes.
- [x] **Heuristics**:
    - [x] Implement a crate-to-target distance heuristic.
    - [x] Improve beyond naive nearest-target Manhattan distance if feasible, such as minimum crate-target assignment.
- [x] **Deadlock and Pruning**:
    - [x] Detect non-goal corner deadlocks.
    - [x] Consider dead squares, frozen crates, or other Sokoban-specific pruning if time permits.
- [x] **Time and Memory Control**:
    - [x] Stop internally before the GUI timeout, preferably around 14.5 seconds.
    - [x] Return a valid solution if one was found before timeout.
    - [x] Keep state storage compact enough for levels with around 2 to 8 crates.

## Phase 4: Evaluation, Report & Submission
- [ ] **Testing and Evaluation**:
    - [ ] Test all provided sample maps.
    - [ ] Test additional Sokoban levels, especially levels with around 2 to 8 crates.
    - [ ] Track solved or unsolved status, solution length, and solving time.
    - [ ] Aim to solve at least 80% of hidden grading levels.
- [ ] **Report**:
    - [ ] Write a report of at most 4 A4 pages.
    - [ ] Describe the SokoBot algorithm with project-specific details.
    - [ ] Explain state representation, action generation, and search-strategy rationale.
    - [ ] Discuss evaluation results, strengths, weaknesses, and example puzzle behavior.
    - [ ] Discuss challenges in automated Sokoban solving.
    - [ ] Include a table of contributions.
    - [ ] Ensure the group writes the report themselves without generative AI writing or writing-improvement help.
- [ ] **Deliverables**:
    - [ ] Submit a zip file containing the full project directory and source files needed to run it.
    - [ ] Submit the report as a PDF.
