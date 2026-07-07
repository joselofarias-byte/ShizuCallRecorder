#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

mkdir -p .ai

if [ -x scripts/ai_index_repo.sh ]; then
  scripts/ai_index_repo.sh >/dev/null 2>&1 || true
fi

{
  echo "# Git Memory"
  echo
  echo "Updated: $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
  echo
  echo "## Branch"
  echo
  echo '```'
  git branch --show-current 2>/dev/null || true
  echo '```'
  echo
  echo "## HEAD"
  echo
  echo '```'
  git rev-parse --short HEAD 2>/dev/null || true
  git log -1 --pretty=format:'%h %s' 2>/dev/null || true
  echo
  echo '```'
  echo
  echo "## Working tree"
  echo
  echo '```'
  git status --short 2>/dev/null || true
  echo '```'
  echo
  echo "## Last diff stat"
  echo
  echo '```'
  git diff --stat HEAD~1..HEAD 2>/dev/null || true
  echo '```'
} > .ai/git_memory.md

echo "OK: .ai/git_memory.md"
