## Candidate Coding Challenge: Add an "Important" Button

### Goal

Add an "Important" button to each TODO item so users can mark/unmark items as important. Important items should be
visually highlighted and appear at the top of their list.

Timebox: You are not limited in the time but I would like to know how long you spent on it.

---

### Acceptance Criteria

- Data model persists importance:
    - New column `important BOOLEAN NOT NULL DEFAULT FALSE` for todo items via Flyway migration.
    - `TodoItem` domain updated with an `important` field.
- Repository and sorting:
    - Queries/order ensure important items list before non‑important, then by `position`.
- API support:
    - Add an endpoint to toggle importance for an item.
- Tests: Add at least 1 unit and 1 integration test for persistence and ordering. No existing tests are broken.

Note: UI surface (button, styling) is the stretch goal below. Focus first on data layer and API.

---

### Scope and Constraints

- Prioritize data and API: migration, domain, repository ordering, service, and controller.
- Commit to git freqently like you would on a production app.
- Keep the UI unchanged for the baseline; expose behavior via API and verify with tests.

---

### Suggested Implementation Path

You do not need to follow this exactly, but it is optimized for the timebox.

1) Migration (data model)

- Create `src/main/resources/db/migration/V3__add_item_important.sql` adding `important BOOLEAN NOT NULL DEFAULT FALSE`
  to the items table.

2) Domain and repository

- Update `src/main/java/com/todoapp/domain/TodoItem.java` to include `important`.
- Update repository queries/ordering so important items sort before non‑important, then by `position`.

3) Service logic

- In `src/main/java/com/todoapp/service/TodoItemService.java`, add `toggleImportant` (or similar) that flips the flag
  and persists it.

4) Controller/API

- In `src/main/java/com/todoapp/web/ItemsController.java`, add an endpoint such as
  `POST /api/lists/{listId}/items/{itemId}/important` that calls the service and returns appropriate status/redirect.

5) Ordering

- Verify the ordering behavior through repository/service or controller tests (important first, then by position).

---

### Stretch Goal (optional)

- UI surface for importance:
    - In `src/main/resources/templates/items.html`, add an "Important" toggle next to each item (text or ★ icon) with
      accessible `aria-label`.
    - Add minimal visual styling for important items in `src/main/resources/static/css/style.css` (e.g., subtle
      background or bold text).
    - For server-rendered flow, the toggle can submit a small form to the controller endpoint and redirect back to
      `/lists/{listId}`.

---

### Files You Will Likely Touch

- `src/main/resources/db/migration/V3__add_item_important.sql`
- `src/main/java/com/todoapp/domain/TodoItem.java`
- `src/main/java/com/todoapp/persistence/*.java` (ordering/query update if applicable)
- `src/main/java/com/todoapp/service/TodoItemService.java`
- `src/main/java/com/todoapp/web/ItemsController.java`
- Tests under `src/test/java/com/todoapp`

Stretch (UI):

- `src/main/resources/templates/items.html`
- `src/main/resources/static/css/style.css`

---

Hints:

- Review existing ordering logic and tests for items to align your implementation.
- Keep controller patterns consistent with existing endpoints (status codes, redirects if using server-rendered flow).

---

### How to Run

```bash
# Run tests
./gradlew test        # macOS/Linux
./gradlew.bat test    # Windows

# Start the app
./gradlew bootRun     # macOS/Linux
./gradlew.bat bootRun # Windows

# Open
# Web UI: http://localhost:8080
# API:    http://localhost:8080/api
```

---

### Evaluation Rubric

- Correctness: Data model persists importance; ordering places important items first; API toggle works.
- Code Quality: Clear, small, well-named methods; minimal surface area; no regressions.
- Tests: At least one passing unit test and one passing integration test focused on persistence/ordering.
- UI (stretch): If implemented, accessible toggle and minimal styling.

---

### Deliverables

- A short PR with:
    - Summary of your approach (baseline vs. stretch)
    - Notes on any trade-offs
    - Instructions to verify manually

Good luck and have fun!


