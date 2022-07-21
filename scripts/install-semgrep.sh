#!/bin/bash -eu

SEMGREP_VERSION=$1
OUTPUT_FILENAME=$2

VENV_DIR=$(mktemp -d)

python3 -m venv --copies "${VENV_DIR}"
. "${VENV_DIR}/bin/activate"
pip install "semgrep==${SEMGREP_VERSION}"

OUTPUT_DIR=$(dirname $OUTPUT_FILENAME)
if [[ ! -d "${OUTPUT_DIR}" ]]; then
  mkdir -p "${OUTPUT_DIR}"
fi

tar czvf "${OUTPUT_FILENAME}" --exclude=*/__pycache__ --exclude=*.pyc -C "${VENV_DIR}" "."