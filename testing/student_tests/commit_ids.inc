I definitions.inc

# get head commit id
> log
===
${COMMIT_HEAD}
${ARBLINE}

===
${COMMIT_HEAD}
${ARBLINE}

===
${COMMIT_HEAD}
${ARBLINE}

===
${COMMIT_HEAD}
${ARBLINE}
<<<*

D MASTER_HEAD_1 "${1}"
D MASTER_HEAD_2 "${2}"
D SPLIT "${3}"
D INIT "${4}"

> checkout new
<<<

> log
===
${COMMIT_HEAD}
${ARBLINE}

===
${COMMIT_HEAD}
${ARBLINE}

${ARBLINES}
<<<*

D NEW_HEAD_1 "${1}"
D NEW_HEAD_2 "${2}"

> checkout master
<<<


