#!/bin/bash

set -euo pipefail
set -x

java src/main/java/org/pq/tool/OS.java > PLATFORM
