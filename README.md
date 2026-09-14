# Campus Puzzle

A university course scheduling project for M603 Advanced Algorithms,
implemented as a Java console application.

The project compares greedy scheduling, Welsh–Powell graph coloring,
dynamic programming for room allocation, and recursive backtracking.

## Setup and Running

Requirements: Java 21 and IntelliJ IDEA with Maven support.
Jackson is used for JSON parsing.

1. Open the project in IntelliJ and select JDK 21.
2. Synchronize Maven dependencies.
3. Run `com.nazlicanguner.campuspuzzle.Main`.

The default input is `data/constraints.json`. To use another dataset,
set **Run → Edit Configurations → Program arguments** to its path:

```text
data/greedy-trap.json
```

The working directory must be the project root. With Maven installed
separately, the project can also be compiled using `mvn compile`.

## Input and Constraints

Inputs contain classes, rooms, student groups, and time slots.
The JSON loader checks required fields, data types, duplicate IDs,
and references to unknown classes.

Each class requires one room and one time slot. A valid assignment must:

- Avoid professor and student-group overlaps.
- Avoid room double-booking.
- Respect room capacity.
- Report every class as either scheduled or unscheduled, with a reason.

Slots are assumed to have equal duration and not overlap. Their labels
are for display; clock times are not parsed. All rooms and professors
are available in every slot, and room suitability depends only on capacity.

## Project Structure

| Package | Purpose |
| --- | --- |
| `model` | Input entities and scheduling results |
| `util` | JSON loading and independent validation |
| `greedy` | Greedy baseline |
| `graph` | Conflict graph and Welsh–Powell coloring |
| `dp` | Fixed-slot room optimization |
| `backtracking` | Bounded recursive search |

`Main` loads the selected dataset, runs the algorithms, prints their
results, and validates the schedules.

## Algorithms and Design Choices

### Greedy

Classes are processed by decreasing enrollment because larger classes
have fewer suitable rooms. The algorithm selects the first feasible
room-slot combination in input order without revisiting earlier choices.

It provides a baseline, but an unscheduled class does not prove that
a complete schedule is impossible.

### Conflict Graph and Welsh–Powell

Classes are connected when they share a professor or student group.
Welsh–Powell processes vertices by decreasing degree and assigns the
same color only to compatible classes. Ties are resolved by class ID.

Colors map to the input time slots by index. This prevents person
conflicts, but does not account for room count or capacity, and does not
guarantee the minimum number of colors.

### Dynamic Programming

For each fixed slot, classes are sorted by enrollment and rooms by
capacity. The DP state stores the best result using the first `i`
classes and first `j` rooms.

Each state considers skipping a class, skipping a room, or assigning
the pair if capacity permits. The objective is to maximize assigned
classes first, then minimize wasted seats.

Under the capacity-only model, feasible assignments can be rearranged
to follow the sorted order without increasing waste. This allows a
two-dimensional DP instead of enumerating room permutations.

DP is applied both to Welsh–Powell slots and, separately, to unchanged
Greedy slots. The latter comparison isolates room-allocation improvements.

### Backtracking

Recursive backtracking explores room-slot assignments, undoes them,
and tries alternatives. It also considers leaving a class unscheduled.

The Greedy result is retained as the initial best solution. Invalid
assignments and branches that cannot improve the best result are pruned.
The objective is scheduled count first, then wasted seats.

The search limit is `200_000L` visited states:

- `Search complete: true`: the optimum is established under the model.
- `Search complete: false`: the best solution found is returned without
  an optimality guarantee.

This is a node limit, not a time or recursion-depth limit.

## Complexity

Let `N` be classes, `R` rooms, `T` slots, `G` student groups,
`E` graph edges, and `c` classes in one slot.
Hash lookups are assumed to take expected constant time.

| Component | Time | Additional space |
| --- | --- | --- |
| Greedy | `O(N log N + N²RT(G+1))` | `O(N)` |
| Graph construction | `O(N²(G+1))` | `O(N+E)` |
| Welsh–Powell | `O(N log N + N² + NE)` upper bound | `O(N)`, excluding graph |
| DP per slot | `O(c log c + R log R + cR)` | `O((c+1)(R+1))` |
| Backtracking search | `O(N(RT+1)^N)` conservative bound | `O(N)`, excluding graph and input |

Backtracking also requires graph construction and Greedy initialization.
DP avoids exponential room-subset enumeration, while backtracking remains
the main scalability limitation.

The recorded datasets contain three or four classes. They demonstrate
behavior, not performance at full university scale.

## Results

Each cell shows **scheduled / unscheduled / wasted seats**.

| Dataset | Greedy | Welsh–Powell + DP | Backtracking |
| --- | --- | --- | --- |
| `constraints.json` | 4 / 0 / 130 | 4 / 0 / 50 | 4 / 0 / 50 |
| `greedy-trap.json` | 3 / 1 / 45 | 4 / 0 / 70 | 4 / 0 / 70 |
| `impossible.json` | 2 / 1 / 25 | 2 / 1 / 25 | 2 / 1 / 25 |
| `room-bottleneck.json` | 3 / 0 / 45 | 2 / 1 / 25 | 3 / 0 / 45 |

Backtracking completed all four searches, visiting 53, 50, 55,
and 58 states respectively.

On `constraints.json`, DP also reduced waste from **130 to 50**
while preserving the Greedy class assignments and time slots:
an approximately **61.5% reduction**.

Waste is unused capacity summed across room-slot assignments, not
measured energy consumption. Fewer wasted seats do not make a result
better if fewer classes are scheduled.

## Validation

`ScheduleValidator` checks results against the original input rather
than reusing the solvers' conflict graph or feasibility methods.

It checks capacity, entity references, unchanged attributes,
room/professor/group conflicts, and whether every class is accounted
for exactly once.

Manual checks covered valid and infeasible schedules, insufficient
capacity, the backtracking node limit, and rejection of a deliberately
duplicated assignment. No automated regression suite has yet been added.

Validation establishes constraint compliance, not optimality.

## Conflict Report and Manual Fix Log

Example from `impossible.json`:

```text
Scheduled | A101 | Monday 09:00-10:00 | R101 | Wasted 10 seats
Scheduled | B101 | Monday 10:00-11:00 | R101 | Wasted 15 seats
Unscheduled | C101 | N/A | N/A | Unscheduled in an optimal partial schedule; the constraints prevent scheduling all classes.
```

All three classes share one professor, who can teach at most two
classes across two slots. At least one class must remain unscheduled.

The following interventions are proposed, not already applied:

| Problem | Possible manual action |
| --- | --- |
| Three classes share a professor across two slots | Add a slot or assign a qualified alternative professor |
| Class exceeds every room's capacity | Provide a larger room or remodel the class as separate sections |
| Too many classes share one color | Move a class to another valid slot, as Backtracking demonstrates |
| Search limit reached | Review the partial result and consider a larger search budget |

After modifying inputs, rerun scheduling and validation. An omitted
class is not necessarily impossible to schedule individually; another
class might need to be omitted instead.
