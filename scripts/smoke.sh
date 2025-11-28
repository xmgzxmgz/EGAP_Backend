#!/usr/bin/env bash
set -euo pipefail
BASE="${BASE_URL:-http://localhost:8080}"

echo "Health:" && curl -s "$BASE/api/health" && echo
echo "Tags:" && curl -s "$BASE/api/tags" && echo
echo "Tags distribution:" && curl -s "$BASE/api/tags/distribution" && echo
echo "Enterprise basic-info:" && curl -s "$BASE/api/enterprise/basic-info" && echo
echo "Enterprise basic-info (制造业):" && curl -s -G --data-urlencode "category=制造业" "$BASE/api/enterprise/basic-info" && echo
echo "Modeling pressure:" && curl -s "$BASE/api/modeling/pressure" && echo
echo "Modeling training:" && curl -s "$BASE/api/modeling/training" && echo
echo "Search q=华:" && curl -s -G --data-urlencode "q=华" "$BASE/api/search" && echo