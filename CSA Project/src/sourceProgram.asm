LOC 10
ZERO:           DATA 0
SEARCH_CHAR:    DATA 0
PARA_PTR:       DATA 0
PARA_LEN:       DATA 0
TMP_PTR:        DATA 0
MAIN:
        LDA R0,0,300,0
        STR R0,0,PARA_PTR,0
        LDR R0,0,ZERO,0
        STR R0,0,PARA_LEN,0
RP_LOOP:
        IN  R0,2
        STR R0,0,PARA_PTR,1
        LDR R2,0,PARA_PTR,0
        AIR R2,1
        STR R2,0,PARA_PTR,0
        LDR R3,0,PARA_LEN,0
        AIR R3,1
        STR R3,0,PARA_LEN,0
        LDR R4,0,ZERO,0
        AIR R4,10
        TRR R0,R4
        JCC 1,RP_DONE,0
        JMA 0,RP_LOOP,0
RP_DONE:
        LDA R0,0,300,0
        STR R0,0,PARA_PTR,0
        LDR R1,0,PARA_LEN,0
        JZ  R1,0,AFTER_PRINT_PARA,0
PP_LOOP:
        LDR R0,0,PARA_PTR,1
        OUT R0,1
        LDR R2,0,PARA_PTR,0
        AIR R2,1
        STR R2,0,PARA_PTR,0
        SOB R1,0,PP_LOOP,0
AFTER_PRINT_PARA:
        IN  R0,0
        STR R0,0,SEARCH_CHAR,0
        LDR R1,0,PARA_LEN,0
        JZ  R1,0,NOT_FOUND,0
        LDR R4,0,SEARCH_CHAR,0
        LDA R0,0,300,0
        STR R0,0,PARA_PTR,0
S_LOOP:
        LDR R0,0,PARA_PTR,1
        TRR R0,R4
        JCC 1,FOUND,0
        LDR R2,0,PARA_PTR,0
        AIR R2,1
        STR R2,0,PARA_PTR,0
        SOB R1,0,S_LOOP,0
        JMA 0,NOT_FOUND,0
FOUND:
        LDA R0,0,340,0
        STR R0,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        HLT
NOT_FOUND:
        LDA R0,0,350,0
        STR R0,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        LDR R2,0,TMP_PTR,0
        AIR R2,1
        STR R2,0,TMP_PTR,0
        LDR R0,0,TMP_PTR,1
        OUT R0,1
        HLT
LOC 340
        DATA 70
        DATA 79
        DATA 85
        DATA 78
        DATA 68
        DATA 10
LOC 350
        DATA 78
        DATA 79
        DATA 84
        DATA 32
        DATA 70
        DATA 79
        DATA 85
        DATA 78
        DATA 68
        DATA 10