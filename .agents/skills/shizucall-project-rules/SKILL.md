# ShizuCallRecorder Project Rules

Use this skill for all work inside ShizuCallRecorder, EverCallRecorder, cally, and related ShizuCallRecorder forks.

## Repository separation

Do not mix Nightzuku work into ShizuCallRecorder.

Nightzuku work belongs only in the Nightzuku fork context.

ShizuCallRecorder work belongs only in the ShizuCallRecorder, EverCallRecorder, cally, and ShizuCallRecorder fork lineage.

## Required working style

Before changing files, inspect the current repository state.

Always identify the current directory, branch, and git status before applying changes.

Prefer small incremental changes.

Avoid unrelated rewrites.

Avoid broad refactors unless explicitly requested.

Preserve existing behavior unless the task explicitly requires behavior changes.

## Android project rules

Do not change package names, signing config, app identity, permissions, services, receivers, or manifest behavior unless explicitly requested.

Do not remove root, Shizuku, call recording, accessibility, foreground service, or notification logic without explicit instruction.

Do not assume phone-only behavior. Check phone, TV, and Wear paths when relevant.

## Build rules

After code changes, prefer running the smallest relevant Gradle task first.

Use full builds only when needed.

If build errors occur, fix the first concrete error before making broader changes.

## Output rules

When giving terminal instructions, provide complete copy-ready commands.

Do not provide fake terminal output.

Do not ask the user to manually edit files when a complete command can generate or patch them.

Keep explanations minimal and focus on executable commands or downloadable artifacts.
