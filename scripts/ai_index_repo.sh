#!/usr/bin/env bash
set -euo pipefail

ROOT="$(git rev-parse --show-toplevel 2>/dev/null || pwd)"
cd "$ROOT"

mkdir -p .ai/index

OUT=".ai/index/repo_index.md"
TMP=".ai/index/repo_index.tmp"

{
  echo "# Repo Index"
  echo
  echo "Generated: $(date -u '+%Y-%m-%d %H:%M:%S UTC')"
  echo
  echo "## Git"
  echo
  echo '```'
  git branch --show-current 2>/dev/null || true
  git rev-parse --short HEAD 2>/dev/null || true
  echo '```'
  echo

  echo "## Recent commits"
  echo
  echo '```'
  git log --oneline -20 2>/dev/null || true
  echo '```'
  echo

  echo "## Important files"
  echo
  find . \
    -type f \
    \( -name '*.kt' -o -name '*.kts' -o -name '*.xml' -o -name '*.gradle' -o -name '*.md' \) \
    ! -path './.git/*' \
    ! -path './.gradle/*' \
    ! -path './build/*' \
    ! -path './*/build/*' \
    ! -path './.idea/*' \
    ! -path './.ai/index/*' \
    | sort
  echo

  echo "## Kotlin symbols"
  echo
  grep -RInE '^[[:space:]]*(class|object|interface|enum class|sealed class|data class|fun)[[:space:]]+[A-Za-z0-9_]+' \
    --include='*.kt' \
    --exclude-dir='.git' \
    --exclude-dir='.gradle' \
    --exclude-dir='build' \
    . 2>/dev/null \
    | sed 's#^\./##' \
    | head -n 2000 || true
  echo

  echo "## Android strings"
  echo
  grep -RInE '<string name=' \
    manager/src/main/res/values* 2>/dev/null \
    | sed 's#^\./##' \
    | head -n 2000 || true
  echo

  echo "## TODO/FIXME"
  echo
  grep -RInE 'TODO|FIXME|HACK|XXX' \
    --include='*.kt' \
    --include='*.xml' \
    --include='*.gradle' \
    --include='*.md' \
    --exclude-dir='.git' \
    --exclude-dir='.gradle' \
    --exclude-dir='build' \
    . 2>/dev/null \
    | sed 's#^\./##' \
    | head -n 1000 || true
} > "$TMP"

mv "$TMP" "$OUT"

echo "OK: $OUT"
wc -l "$OUT"
