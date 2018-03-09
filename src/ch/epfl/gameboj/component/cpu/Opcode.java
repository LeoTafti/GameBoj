package ch.epfl.gameboj.component.cpu;

public enum Opcode {
    // Direct (non-prefixed) opcodes
    ADD_A_B(Kind.DIRECT, Family.ADD_A_R8, 0x80, 1, 1),
    ADD_A_C(Kind.DIRECT, Family.ADD_A_R8, 0x81, 1, 1),
    ADD_A_D(Kind.DIRECT, Family.ADD_A_R8, 0x82, 1, 1),
    ADD_A_E(Kind.DIRECT, Family.ADD_A_R8, 0x83, 1, 1),
    ADD_A_H(Kind.DIRECT, Family.ADD_A_R8, 0x84, 1, 1),
    ADD_A_L(Kind.DIRECT, Family.ADD_A_R8, 0x85, 1, 1),
    ADD_A_A(Kind.DIRECT, Family.ADD_A_R8, 0x87, 1, 1),
    ADC_A_B(Kind.DIRECT, Family.ADD_A_R8, 0x88, 1, 1),
    ADC_A_C(Kind.DIRECT, Family.ADD_A_R8, 0x89, 1, 1),
    ADC_A_D(Kind.DIRECT, Family.ADD_A_R8, 0x8A, 1, 1),
    ADC_A_E(Kind.DIRECT, Family.ADD_A_R8, 0x8B, 1, 1),
    ADC_A_H(Kind.DIRECT, Family.ADD_A_R8, 0x8C, 1, 1),
    ADC_A_L(Kind.DIRECT, Family.ADD_A_R8, 0x8D, 1, 1),
    ADC_A_A(Kind.DIRECT, Family.ADD_A_R8, 0x8F, 1, 1),
    SUB_A_B(Kind.DIRECT, Family.SUB_A_R8, 0x90, 1, 1),
    SUB_A_C(Kind.DIRECT, Family.SUB_A_R8, 0x91, 1, 1),
    SUB_A_D(Kind.DIRECT, Family.SUB_A_R8, 0x92, 1, 1),
    SUB_A_E(Kind.DIRECT, Family.SUB_A_R8, 0x93, 1, 1),
    SUB_A_H(Kind.DIRECT, Family.SUB_A_R8, 0x94, 1, 1),
    SUB_A_L(Kind.DIRECT, Family.SUB_A_R8, 0x95, 1, 1),
    SUB_A_A(Kind.DIRECT, Family.SUB_A_R8, 0x97, 1, 1),
    SBC_A_B(Kind.DIRECT, Family.SUB_A_R8, 0x98, 1, 1),
    SBC_A_C(Kind.DIRECT, Family.SUB_A_R8, 0x99, 1, 1),
    SBC_A_D(Kind.DIRECT, Family.SUB_A_R8, 0x9A, 1, 1),
    SBC_A_E(Kind.DIRECT, Family.SUB_A_R8, 0x9B, 1, 1),
    SBC_A_H(Kind.DIRECT, Family.SUB_A_R8, 0x9C, 1, 1),
    SBC_A_L(Kind.DIRECT, Family.SUB_A_R8, 0x9D, 1, 1),
    SBC_A_A(Kind.DIRECT, Family.SUB_A_R8, 0x9F, 1, 1),
    AND_A_B(Kind.DIRECT, Family.AND_A_R8, 0xA0, 1, 1),
    AND_A_C(Kind.DIRECT, Family.AND_A_R8, 0xA1, 1, 1),
    AND_A_D(Kind.DIRECT, Family.AND_A_R8, 0xA2, 1, 1),
    AND_A_E(Kind.DIRECT, Family.AND_A_R8, 0xA3, 1, 1),
    AND_A_H(Kind.DIRECT, Family.AND_A_R8, 0xA4, 1, 1),
    AND_A_L(Kind.DIRECT, Family.AND_A_R8, 0xA5, 1, 1),
    AND_A_A(Kind.DIRECT, Family.AND_A_R8, 0xA7, 1, 1),
    OR_A_B(Kind.DIRECT, Family.OR_A_R8, 0xB0, 1, 1),
    OR_A_C(Kind.DIRECT, Family.OR_A_R8, 0xB1, 1, 1),
    OR_A_D(Kind.DIRECT, Family.OR_A_R8, 0xB2, 1, 1),
    OR_A_E(Kind.DIRECT, Family.OR_A_R8, 0xB3, 1, 1),
    OR_A_H(Kind.DIRECT, Family.OR_A_R8, 0xB4, 1, 1),
    OR_A_L(Kind.DIRECT, Family.OR_A_R8, 0xB5, 1, 1),
    OR_A_A(Kind.DIRECT, Family.OR_A_R8, 0xB7, 1, 1),
    XOR_A_B(Kind.DIRECT, Family.XOR_A_R8, 0xA8, 1, 1),
    XOR_A_C(Kind.DIRECT, Family.XOR_A_R8, 0xA9, 1, 1),
    XOR_A_D(Kind.DIRECT, Family.XOR_A_R8, 0xAA, 1, 1),
    XOR_A_E(Kind.DIRECT, Family.XOR_A_R8, 0xAB, 1, 1),
    XOR_A_H(Kind.DIRECT, Family.XOR_A_R8, 0xAC, 1, 1),
    XOR_A_L(Kind.DIRECT, Family.XOR_A_R8, 0xAD, 1, 1),
    XOR_A_A(Kind.DIRECT, Family.XOR_A_R8, 0xAF, 1, 1),
    CP_A_B(Kind.DIRECT, Family.CP_A_R8, 0xB8, 1, 1),
    CP_A_C(Kind.DIRECT, Family.CP_A_R8, 0xB9, 1, 1),
    CP_A_D(Kind.DIRECT, Family.CP_A_R8, 0xBA, 1, 1),
    CP_A_E(Kind.DIRECT, Family.CP_A_R8, 0xBB, 1, 1),
    CP_A_H(Kind.DIRECT, Family.CP_A_R8, 0xBC, 1, 1),
    CP_A_L(Kind.DIRECT, Family.CP_A_R8, 0xBD, 1, 1),
    CP_A_A(Kind.DIRECT, Family.CP_A_R8, 0xBF, 1, 1),
    ADD_A_N8(Kind.DIRECT, Family.ADD_A_N8, 0xC6, 2, 2),
    ADC_A_N8(Kind.DIRECT, Family.ADD_A_N8, 0xCE, 2, 2),
    SUB_A_N8(Kind.DIRECT, Family.SUB_A_N8, 0xD6, 2, 2),
    SBC_A_N8(Kind.DIRECT, Family.SUB_A_N8, 0xDE, 2, 2),
    AND_A_N8(Kind.DIRECT, Family.AND_A_N8, 0xE6, 2, 2),
    OR_A_N8(Kind.DIRECT, Family.OR_A_N8, 0xF6, 2, 2),
    XOR_A_N8(Kind.DIRECT, Family.XOR_A_N8, 0xEE, 2, 2),
    CP_A_N8(Kind.DIRECT, Family.CP_A_N8, 0xFE, 2, 2),
    ADD_A_HLR(Kind.DIRECT, Family.ADD_A_HLR, 0x86, 1, 2),
    ADC_A_HLR(Kind.DIRECT, Family.ADD_A_HLR, 0x8E, 1, 2),
    SUB_A_HLR(Kind.DIRECT, Family.SUB_A_HLR, 0x96, 1, 2),
    SBC_A_HLR(Kind.DIRECT, Family.SUB_A_HLR, 0x9E, 1, 2),
    AND_A_HLR(Kind.DIRECT, Family.AND_A_HLR, 0xA6, 1, 2),
    OR_A_HLR(Kind.DIRECT, Family.OR_A_HLR, 0xB6, 1, 2),
    XOR_A_HLR(Kind.DIRECT, Family.XOR_A_HLR, 0xAE, 1, 2),
    CP_A_HLR(Kind.DIRECT, Family.CP_A_HLR, 0xBE, 1, 2),
    PUSH_BC(Kind.DIRECT, Family.PUSH_R16, 0xC5, 1, 4),
    PUSH_DE(Kind.DIRECT, Family.PUSH_R16, 0xD5, 1, 4),
    PUSH_HL(Kind.DIRECT, Family.PUSH_R16, 0xE5, 1, 4),
    PUSH_AF(Kind.DIRECT, Family.PUSH_R16, 0xF5, 1, 4),
    POP_BC(Kind.DIRECT, Family.POP_R16, 0xC1, 1, 3),
    POP_DE(Kind.DIRECT, Family.POP_R16, 0xD1, 1, 3),
    POP_HL(Kind.DIRECT, Family.POP_R16, 0xE1, 1, 3),
    POP_AF(Kind.DIRECT, Family.POP_R16, 0xF1, 1, 3),
    INC_B(Kind.DIRECT, Family.INC_R8, 0x04, 1, 1),
    INC_C(Kind.DIRECT, Family.INC_R8, 0x0C, 1, 1),
    INC_D(Kind.DIRECT, Family.INC_R8, 0x14, 1, 1),
    INC_E(Kind.DIRECT, Family.INC_R8, 0x1C, 1, 1),
    INC_H(Kind.DIRECT, Family.INC_R8, 0x24, 1, 1),
    INC_L(Kind.DIRECT, Family.INC_R8, 0x2C, 1, 1),
    INC_A(Kind.DIRECT, Family.INC_R8, 0x3C, 1, 1),
    DEC_B(Kind.DIRECT, Family.DEC_R8, 0x05, 1, 1),
    DEC_C(Kind.DIRECT, Family.DEC_R8, 0x0D, 1, 1),
    DEC_D(Kind.DIRECT, Family.DEC_R8, 0x15, 1, 1),
    DEC_E(Kind.DIRECT, Family.DEC_R8, 0x1D, 1, 1),
    DEC_H(Kind.DIRECT, Family.DEC_R8, 0x25, 1, 1),
    DEC_L(Kind.DIRECT, Family.DEC_R8, 0x2D, 1, 1),
    DEC_A(Kind.DIRECT, Family.DEC_R8, 0x3D, 1, 1),
    INC_BC(Kind.DIRECT, Family.INC_R16SP, 0x03, 1, 2),
    INC_DE(Kind.DIRECT, Family.INC_R16SP, 0x13, 1, 2),
    INC_HL(Kind.DIRECT, Family.INC_R16SP, 0x23, 1, 2),
    INC_SP(Kind.DIRECT, Family.INC_R16SP, 0x33, 1, 2),
    DEC_BC(Kind.DIRECT, Family.DEC_R16SP, 0x0B, 1, 2),
    DEC_DE(Kind.DIRECT, Family.DEC_R16SP, 0x1B, 1, 2),
    DEC_HL(Kind.DIRECT, Family.DEC_R16SP, 0x2B, 1, 2),
    DEC_SP(Kind.DIRECT, Family.DEC_R16SP, 0x3B, 1, 2),
    INC_HLR(Kind.DIRECT, Family.INC_HLR, 0x34, 1, 3),
    DEC_HLR(Kind.DIRECT, Family.DEC_HLR, 0x35, 1, 3),
    RLCA(Kind.DIRECT, Family.ROTCA, 0x07, 1, 1),
    RRCA(Kind.DIRECT, Family.ROTCA, 0x0F, 1, 1),
    RLA(Kind.DIRECT, Family.ROTA, 0x17, 1, 1),
    RRA(Kind.DIRECT, Family.ROTA, 0x1F, 1, 1),
    LD_B_B(Kind.DIRECT, Family.NOP, 0x40, 1, 1),
    LD_B_C(Kind.DIRECT, Family.LD_R8_R8, 0x41, 1, 1),
    LD_B_D(Kind.DIRECT, Family.LD_R8_R8, 0x42, 1, 1),
    LD_B_E(Kind.DIRECT, Family.LD_R8_R8, 0x43, 1, 1),
    LD_B_H(Kind.DIRECT, Family.LD_R8_R8, 0x44, 1, 1),
    LD_B_L(Kind.DIRECT, Family.LD_R8_R8, 0x45, 1, 1),
    LD_B_A(Kind.DIRECT, Family.LD_R8_R8, 0x47, 1, 1),
    LD_C_B(Kind.DIRECT, Family.LD_R8_R8, 0x48, 1, 1),
    LD_C_C(Kind.DIRECT, Family.NOP, 0x49, 1, 1),
    LD_C_D(Kind.DIRECT, Family.LD_R8_R8, 0x4A, 1, 1),
    LD_C_E(Kind.DIRECT, Family.LD_R8_R8, 0x4B, 1, 1),
    LD_C_H(Kind.DIRECT, Family.LD_R8_R8, 0x4C, 1, 1),
    LD_C_L(Kind.DIRECT, Family.LD_R8_R8, 0x4D, 1, 1),
    LD_C_A(Kind.DIRECT, Family.LD_R8_R8, 0x4F, 1, 1),
    LD_D_B(Kind.DIRECT, Family.LD_R8_R8, 0x50, 1, 1),
    LD_D_C(Kind.DIRECT, Family.LD_R8_R8, 0x51, 1, 1),
    LD_D_D(Kind.DIRECT, Family.NOP, 0x52, 1, 1),
    LD_D_E(Kind.DIRECT, Family.LD_R8_R8, 0x53, 1, 1),
    LD_D_H(Kind.DIRECT, Family.LD_R8_R8, 0x54, 1, 1),
    LD_D_L(Kind.DIRECT, Family.LD_R8_R8, 0x55, 1, 1),
    LD_D_A(Kind.DIRECT, Family.LD_R8_R8, 0x57, 1, 1),
    LD_E_B(Kind.DIRECT, Family.LD_R8_R8, 0x58, 1, 1),
    LD_E_C(Kind.DIRECT, Family.LD_R8_R8, 0x59, 1, 1),
    LD_E_D(Kind.DIRECT, Family.LD_R8_R8, 0x5A, 1, 1),
    LD_E_E(Kind.DIRECT, Family.NOP, 0x5B, 1, 1),
    LD_E_H(Kind.DIRECT, Family.LD_R8_R8, 0x5C, 1, 1),
    LD_E_L(Kind.DIRECT, Family.LD_R8_R8, 0x5D, 1, 1),
    LD_E_A(Kind.DIRECT, Family.LD_R8_R8, 0x5F, 1, 1),
    LD_H_B(Kind.DIRECT, Family.LD_R8_R8, 0x60, 1, 1),
    LD_H_C(Kind.DIRECT, Family.LD_R8_R8, 0x61, 1, 1),
    LD_H_D(Kind.DIRECT, Family.LD_R8_R8, 0x62, 1, 1),
    LD_H_E(Kind.DIRECT, Family.LD_R8_R8, 0x63, 1, 1),
    LD_H_H(Kind.DIRECT, Family.NOP, 0x64, 1, 1),
    LD_H_L(Kind.DIRECT, Family.LD_R8_R8, 0x65, 1, 1),
    LD_H_A(Kind.DIRECT, Family.LD_R8_R8, 0x67, 1, 1),
    LD_L_B(Kind.DIRECT, Family.LD_R8_R8, 0x68, 1, 1),
    LD_L_C(Kind.DIRECT, Family.LD_R8_R8, 0x69, 1, 1),
    LD_L_D(Kind.DIRECT, Family.LD_R8_R8, 0x6A, 1, 1),
    LD_L_E(Kind.DIRECT, Family.LD_R8_R8, 0x6B, 1, 1),
    LD_L_H(Kind.DIRECT, Family.LD_R8_R8, 0x6C, 1, 1),
    LD_L_L(Kind.DIRECT, Family.NOP, 0x6D, 1, 1),
    LD_L_A(Kind.DIRECT, Family.LD_R8_R8, 0x6F, 1, 1),
    LD_A_B(Kind.DIRECT, Family.LD_R8_R8, 0x78, 1, 1),
    LD_A_C(Kind.DIRECT, Family.LD_R8_R8, 0x79, 1, 1),
    LD_A_D(Kind.DIRECT, Family.LD_R8_R8, 0x7A, 1, 1),
    LD_A_E(Kind.DIRECT, Family.LD_R8_R8, 0x7B, 1, 1),
    LD_A_H(Kind.DIRECT, Family.LD_R8_R8, 0x7C, 1, 1),
    LD_A_L(Kind.DIRECT, Family.LD_R8_R8, 0x7D, 1, 1),
    LD_A_A(Kind.DIRECT, Family.NOP, 0x7F, 1, 1),
    LD_B_N8(Kind.DIRECT, Family.LD_R8_N8, 0x06, 2, 2),
    LD_C_N8(Kind.DIRECT, Family.LD_R8_N8, 0x0E, 2, 2),
    LD_D_N8(Kind.DIRECT, Family.LD_R8_N8, 0x16, 2, 2),
    LD_E_N8(Kind.DIRECT, Family.LD_R8_N8, 0x1E, 2, 2),
    LD_H_N8(Kind.DIRECT, Family.LD_R8_N8, 0x26, 2, 2),
    LD_L_N8(Kind.DIRECT, Family.LD_R8_N8, 0x2E, 2, 2),
    LD_A_N8(Kind.DIRECT, Family.LD_R8_N8, 0x3E, 2, 2),
    LD_B_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x46, 1, 2),
    LD_C_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x4E, 1, 2),
    LD_D_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x56, 1, 2),
    LD_E_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x5E, 1, 2),
    LD_H_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x66, 1, 2),
    LD_L_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x6E, 1, 2),
    LD_A_HLR(Kind.DIRECT, Family.LD_R8_HLR, 0x7E, 1, 2),
    LD_BC_N16(Kind.DIRECT, Family.LD_R16SP_N16, 0x01, 3, 3),
    LD_DE_N16(Kind.DIRECT, Family.LD_R16SP_N16, 0x11, 3, 3),
    LD_HL_N16(Kind.DIRECT, Family.LD_R16SP_N16, 0x21, 3, 3),
    LD_SP_N16(Kind.DIRECT, Family.LD_R16SP_N16, 0x31, 3, 3),
    LD_HLR_B(Kind.DIRECT, Family.LD_HLR_R8, 0x70, 1, 2),
    LD_HLR_C(Kind.DIRECT, Family.LD_HLR_R8, 0x71, 1, 2),
    LD_HLR_D(Kind.DIRECT, Family.LD_HLR_R8, 0x72, 1, 2),
    LD_HLR_E(Kind.DIRECT, Family.LD_HLR_R8, 0x73, 1, 2),
    LD_HLR_H(Kind.DIRECT, Family.LD_HLR_R8, 0x74, 1, 2),
    LD_HLR_L(Kind.DIRECT, Family.LD_HLR_R8, 0x75, 1, 2),
    LD_HLR_A(Kind.DIRECT, Family.LD_HLR_R8, 0x77, 1, 2),
    ADD_HL_BC(Kind.DIRECT, Family.ADD_HL_R16SP, 0x09, 1, 2),
    ADD_HL_DE(Kind.DIRECT, Family.ADD_HL_R16SP, 0x19, 1, 2),
    ADD_HL_HL(Kind.DIRECT, Family.ADD_HL_R16SP, 0x29, 1, 2),
    ADD_HL_SP(Kind.DIRECT, Family.ADD_HL_R16SP, 0x39, 1, 2),
    JP_NZ_N16(Kind.DIRECT, Family.JP_CC_N16, 0xC2, 3, 3),
    JP_Z_N16(Kind.DIRECT, Family.JP_CC_N16, 0xCA, 3, 3),
    JP_NC_N16(Kind.DIRECT, Family.JP_CC_N16, 0xD2, 3, 3),
    JP_C_N16(Kind.DIRECT, Family.JP_CC_N16, 0xDA, 3, 3),
    JR_NZ_E8(Kind.DIRECT, Family.JR_CC_E8, 0x20, 2, 2),
    JR_Z_E8(Kind.DIRECT, Family.JR_CC_E8, 0x28, 2, 2),
    JR_NC_E8(Kind.DIRECT, Family.JR_CC_E8, 0x30, 2, 2),
    JR_C_E8(Kind.DIRECT, Family.JR_CC_E8, 0x38, 2, 2),
    CALL_NZ_N16(Kind.DIRECT, Family.CALL_CC_N16, 0xC4, 3, 3),
    CALL_Z_N16(Kind.DIRECT, Family.CALL_CC_N16, 0xCC, 3, 3),
    CALL_NC_N16(Kind.DIRECT, Family.CALL_CC_N16, 0xD4, 3, 3),
    CALL_C_N16(Kind.DIRECT, Family.CALL_CC_N16, 0xDC, 3, 3),
    RET_NZ(Kind.DIRECT, Family.RET_CC, 0xC0, 1, 2),
    RET_Z(Kind.DIRECT, Family.RET_CC, 0xC8, 1, 2),
    RET_NC(Kind.DIRECT, Family.RET_CC, 0xD0, 1, 2),
    RET_C(Kind.DIRECT, Family.RET_CC, 0xD8, 1, 2),
    RST_0(Kind.DIRECT, Family.RST_U3, 0xC7, 1, 4),
    RST_1(Kind.DIRECT, Family.RST_U3, 0xCF, 1, 4),
    RST_2(Kind.DIRECT, Family.RST_U3, 0xD7, 1, 4),
    RST_3(Kind.DIRECT, Family.RST_U3, 0xDF, 1, 4),
    RST_4(Kind.DIRECT, Family.RST_U3, 0xE7, 1, 4),
    RST_5(Kind.DIRECT, Family.RST_U3, 0xEF, 1, 4),
    RST_6(Kind.DIRECT, Family.RST_U3, 0xF7, 1, 4),
    RST_7(Kind.DIRECT, Family.RST_U3, 0xFF, 1, 4),
    LD_SP_HL(Kind.DIRECT, Family.LD_SP_HL, 0xF9, 1, 2),
    LD_A_HLRI(Kind.DIRECT, Family.LD_A_HLRU, 0x2A, 1, 2),
    LD_A_HLRD(Kind.DIRECT, Family.LD_A_HLRU, 0x3A, 1, 2),
    LD_A_BCR(Kind.DIRECT, Family.LD_A_BCR, 0x0A, 1, 2),
    LD_A_DER(Kind.DIRECT, Family.LD_A_DER, 0x1A, 1, 2),
    LD_A_CR(Kind.DIRECT, Family.LD_A_CR, 0xF2, 1, 2),
    LD_HLRI_A(Kind.DIRECT, Family.LD_HLRU_A, 0x22, 1, 2),
    LD_HLRD_A(Kind.DIRECT, Family.LD_HLRU_A, 0x32, 1, 2),
    LD_BCR_A(Kind.DIRECT, Family.LD_BCR_A, 0x02, 1, 2),
    LD_DER_A(Kind.DIRECT, Family.LD_DER_A, 0x12, 1, 2),
    LD_CR_A(Kind.DIRECT, Family.LD_CR_A, 0xE2, 1, 2),
    JP_HL(Kind.DIRECT, Family.JP_HL, 0xE9, 1, 1),
    RET(Kind.DIRECT, Family.RET, 0xC9, 1, 4),
    RETI(Kind.DIRECT, Family.RETI, 0xD9, 1, 4),
    DAA(Kind.DIRECT, Family.DAA, 0x27, 1, 1),
    CPL(Kind.DIRECT, Family.CPL, 0x2F, 1, 1),
    NOP(Kind.DIRECT, Family.NOP, 0x00, 1, 1),
    HALT(Kind.DIRECT, Family.HALT, 0x76, 1, 0),
    STOP(Kind.DIRECT, Family.STOP, 0x10, 1, 0),
    CCF(Kind.DIRECT, Family.SCCF, 0x3F, 1, 1),
    SCF(Kind.DIRECT, Family.SCCF, 0x37, 1, 1),
    DI(Kind.DIRECT, Family.EDI, 0xF3, 1, 1),
    EI(Kind.DIRECT, Family.EDI, 0xFB, 1, 1),
    LD_A_N8R(Kind.DIRECT, Family.LD_A_N8R, 0xF0, 2, 3),
    LD_HL_SP_N8(Kind.DIRECT, Family.LD_HLSP_S8, 0xF8, 2, 3),
    LD_HLR_N8(Kind.DIRECT, Family.LD_HLR_N8, 0x36, 2, 3),
    LD_N8R_A(Kind.DIRECT, Family.LD_N8R_A, 0xE0, 2, 3),
    ADD_SP_N(Kind.DIRECT, Family.LD_HLSP_S8, 0xE8, 2, 4),
    LD_A_N16R(Kind.DIRECT, Family.LD_A_N16R, 0xFA, 3, 4),
    LD_N16R_A(Kind.DIRECT, Family.LD_N16R_A, 0xEA, 3, 4),
    LD_N16R_SP(Kind.DIRECT, Family.LD_N16R_SP, 0x08, 3, 5),
    JP_N16(Kind.DIRECT, Family.JP_N16, 0xC3, 3, 4),
    CALL_N16(Kind.DIRECT, Family.CALL_N16, 0xCD, 3, 6),
    JR_E8(Kind.DIRECT, Family.JR_E8, 0x18, 2, 3),

    // Prefixed opcodes
    RLC_B(Kind.PREFIXED, Family.ROTC_R8, 0x00, 2, 2),
    RLC_C(Kind.PREFIXED, Family.ROTC_R8, 0x01, 2, 2),
    RLC_D(Kind.PREFIXED, Family.ROTC_R8, 0x02, 2, 2),
    RLC_E(Kind.PREFIXED, Family.ROTC_R8, 0x03, 2, 2),
    RLC_H(Kind.PREFIXED, Family.ROTC_R8, 0x04, 2, 2),
    RLC_L(Kind.PREFIXED, Family.ROTC_R8, 0x05, 2, 2),
    RLC_A(Kind.PREFIXED, Family.ROTC_R8, 0x07, 2, 2),
    RRC_B(Kind.PREFIXED, Family.ROTC_R8, 0x08, 2, 2),
    RRC_C(Kind.PREFIXED, Family.ROTC_R8, 0x09, 2, 2),
    RRC_D(Kind.PREFIXED, Family.ROTC_R8, 0x0A, 2, 2),
    RRC_E(Kind.PREFIXED, Family.ROTC_R8, 0x0B, 2, 2),
    RRC_H(Kind.PREFIXED, Family.ROTC_R8, 0x0C, 2, 2),
    RRC_L(Kind.PREFIXED, Family.ROTC_R8, 0x0D, 2, 2),
    RRC_A(Kind.PREFIXED, Family.ROTC_R8, 0x0F, 2, 2),
    RL_B(Kind.PREFIXED, Family.ROT_R8, 0x10, 2, 2),
    RL_C(Kind.PREFIXED, Family.ROT_R8, 0x11, 2, 2),
    RL_D(Kind.PREFIXED, Family.ROT_R8, 0x12, 2, 2),
    RL_E(Kind.PREFIXED, Family.ROT_R8, 0x13, 2, 2),
    RL_H(Kind.PREFIXED, Family.ROT_R8, 0x14, 2, 2),
    RL_L(Kind.PREFIXED, Family.ROT_R8, 0x15, 2, 2),
    RL_A(Kind.PREFIXED, Family.ROT_R8, 0x17, 2, 2),
    RR_B(Kind.PREFIXED, Family.ROT_R8, 0x18, 2, 2),
    RR_C(Kind.PREFIXED, Family.ROT_R8, 0x19, 2, 2),
    RR_D(Kind.PREFIXED, Family.ROT_R8, 0x1A, 2, 2),
    RR_E(Kind.PREFIXED, Family.ROT_R8, 0x1B, 2, 2),
    RR_H(Kind.PREFIXED, Family.ROT_R8, 0x1C, 2, 2),
    RR_L(Kind.PREFIXED, Family.ROT_R8, 0x1D, 2, 2),
    RR_A(Kind.PREFIXED, Family.ROT_R8, 0x1F, 2, 2),
    SLA_B(Kind.PREFIXED, Family.SLA_R8, 0x20, 2, 2),
    SLA_C(Kind.PREFIXED, Family.SLA_R8, 0x21, 2, 2),
    SLA_D(Kind.PREFIXED, Family.SLA_R8, 0x22, 2, 2),
    SLA_E(Kind.PREFIXED, Family.SLA_R8, 0x23, 2, 2),
    SLA_H(Kind.PREFIXED, Family.SLA_R8, 0x24, 2, 2),
    SLA_L(Kind.PREFIXED, Family.SLA_R8, 0x25, 2, 2),
    SLA_A(Kind.PREFIXED, Family.SLA_R8, 0x27, 2, 2),
    SRA_B(Kind.PREFIXED, Family.SRA_R8, 0x28, 2, 2),
    SRA_C(Kind.PREFIXED, Family.SRA_R8, 0x29, 2, 2),
    SRA_D(Kind.PREFIXED, Family.SRA_R8, 0x2A, 2, 2),
    SRA_E(Kind.PREFIXED, Family.SRA_R8, 0x2B, 2, 2),
    SRA_H(Kind.PREFIXED, Family.SRA_R8, 0x2C, 2, 2),
    SRA_L(Kind.PREFIXED, Family.SRA_R8, 0x2D, 2, 2),
    SRA_A(Kind.PREFIXED, Family.SRA_R8, 0x2F, 2, 2),
    SWAP_B(Kind.PREFIXED, Family.SWAP_R8, 0x30, 2, 2),
    SWAP_C(Kind.PREFIXED, Family.SWAP_R8, 0x31, 2, 2),
    SWAP_D(Kind.PREFIXED, Family.SWAP_R8, 0x32, 2, 2),
    SWAP_E(Kind.PREFIXED, Family.SWAP_R8, 0x33, 2, 2),
    SWAP_H(Kind.PREFIXED, Family.SWAP_R8, 0x34, 2, 2),
    SWAP_L(Kind.PREFIXED, Family.SWAP_R8, 0x35, 2, 2),
    SWAP_A(Kind.PREFIXED, Family.SWAP_R8, 0x37, 2, 2),
    SRL_B(Kind.PREFIXED, Family.SRL_R8, 0x38, 2, 2),
    SRL_C(Kind.PREFIXED, Family.SRL_R8, 0x39, 2, 2),
    SRL_D(Kind.PREFIXED, Family.SRL_R8, 0x3A, 2, 2),
    SRL_E(Kind.PREFIXED, Family.SRL_R8, 0x3B, 2, 2),
    SRL_H(Kind.PREFIXED, Family.SRL_R8, 0x3C, 2, 2),
    SRL_L(Kind.PREFIXED, Family.SRL_R8, 0x3D, 2, 2),
    SRL_A(Kind.PREFIXED, Family.SRL_R8, 0x3F, 2, 2),
    RLC_HLR(Kind.PREFIXED, Family.ROTC_HLR, 0x06, 2, 4),
    RRC_HLR(Kind.PREFIXED, Family.ROTC_HLR, 0x0E, 2, 4),
    RL_HLR(Kind.PREFIXED, Family.ROT_HLR, 0x16, 2, 4),
    RR_HLR(Kind.PREFIXED, Family.ROT_HLR, 0x1E, 2, 4),
    SLA_HLR(Kind.PREFIXED, Family.SLA_HLR, 0x26, 2, 4),
    SRA_HLR(Kind.PREFIXED, Family.SRA_HLR, 0x2E, 2, 4),
    SWAP_HLR(Kind.PREFIXED, Family.SWAP_HLR, 0x36, 2, 4),
    SRL_HLR(Kind.PREFIXED, Family.SRL_HLR, 0x3E, 2, 4),
    BIT_0_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x40, 2, 2),
    BIT_0_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x41, 2, 2),
    BIT_0_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x42, 2, 2),
    BIT_0_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x43, 2, 2),
    BIT_0_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x44, 2, 2),
    BIT_0_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x45, 2, 2),
    BIT_0_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x47, 2, 2),
    BIT_1_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x48, 2, 2),
    BIT_1_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x49, 2, 2),
    BIT_1_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x4A, 2, 2),
    BIT_1_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x4B, 2, 2),
    BIT_1_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x4C, 2, 2),
    BIT_1_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x4D, 2, 2),
    BIT_1_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x4F, 2, 2),
    BIT_2_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x50, 2, 2),
    BIT_2_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x51, 2, 2),
    BIT_2_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x52, 2, 2),
    BIT_2_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x53, 2, 2),
    BIT_2_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x54, 2, 2),
    BIT_2_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x55, 2, 2),
    BIT_2_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x57, 2, 2),
    BIT_3_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x58, 2, 2),
    BIT_3_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x59, 2, 2),
    BIT_3_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x5A, 2, 2),
    BIT_3_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x5B, 2, 2),
    BIT_3_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x5C, 2, 2),
    BIT_3_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x5D, 2, 2),
    BIT_3_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x5F, 2, 2),
    BIT_4_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x60, 2, 2),
    BIT_4_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x61, 2, 2),
    BIT_4_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x62, 2, 2),
    BIT_4_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x63, 2, 2),
    BIT_4_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x64, 2, 2),
    BIT_4_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x65, 2, 2),
    BIT_4_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x67, 2, 2),
    BIT_5_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x68, 2, 2),
    BIT_5_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x69, 2, 2),
    BIT_5_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x6A, 2, 2),
    BIT_5_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x6B, 2, 2),
    BIT_5_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x6C, 2, 2),
    BIT_5_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x6D, 2, 2),
    BIT_5_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x6F, 2, 2),
    BIT_6_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x70, 2, 2),
    BIT_6_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x71, 2, 2),
    BIT_6_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x72, 2, 2),
    BIT_6_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x73, 2, 2),
    BIT_6_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x74, 2, 2),
    BIT_6_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x75, 2, 2),
    BIT_6_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x77, 2, 2),
    BIT_7_B(Kind.PREFIXED, Family.BIT_U3_R8, 0x78, 2, 2),
    BIT_7_C(Kind.PREFIXED, Family.BIT_U3_R8, 0x79, 2, 2),
    BIT_7_D(Kind.PREFIXED, Family.BIT_U3_R8, 0x7A, 2, 2),
    BIT_7_E(Kind.PREFIXED, Family.BIT_U3_R8, 0x7B, 2, 2),
    BIT_7_H(Kind.PREFIXED, Family.BIT_U3_R8, 0x7C, 2, 2),
    BIT_7_L(Kind.PREFIXED, Family.BIT_U3_R8, 0x7D, 2, 2),
    BIT_7_A(Kind.PREFIXED, Family.BIT_U3_R8, 0x7F, 2, 2),
    RES_0_B(Kind.PREFIXED, Family.CHG_U3_R8, 0x80, 2, 2),
    RES_0_C(Kind.PREFIXED, Family.CHG_U3_R8, 0x81, 2, 2),
    RES_0_D(Kind.PREFIXED, Family.CHG_U3_R8, 0x82, 2, 2),
    RES_0_E(Kind.PREFIXED, Family.CHG_U3_R8, 0x83, 2, 2),
    RES_0_H(Kind.PREFIXED, Family.CHG_U3_R8, 0x84, 2, 2),
    RES_0_L(Kind.PREFIXED, Family.CHG_U3_R8, 0x85, 2, 2),
    RES_0_A(Kind.PREFIXED, Family.CHG_U3_R8, 0x87, 2, 2),
    RES_1_B(Kind.PREFIXED, Family.CHG_U3_R8, 0x88, 2, 2),
    RES_1_C(Kind.PREFIXED, Family.CHG_U3_R8, 0x89, 2, 2),
    RES_1_D(Kind.PREFIXED, Family.CHG_U3_R8, 0x8A, 2, 2),
    RES_1_E(Kind.PREFIXED, Family.CHG_U3_R8, 0x8B, 2, 2),
    RES_1_H(Kind.PREFIXED, Family.CHG_U3_R8, 0x8C, 2, 2),
    RES_1_L(Kind.PREFIXED, Family.CHG_U3_R8, 0x8D, 2, 2),
    RES_1_A(Kind.PREFIXED, Family.CHG_U3_R8, 0x8F, 2, 2),
    RES_2_B(Kind.PREFIXED, Family.CHG_U3_R8, 0x90, 2, 2),
    RES_2_C(Kind.PREFIXED, Family.CHG_U3_R8, 0x91, 2, 2),
    RES_2_D(Kind.PREFIXED, Family.CHG_U3_R8, 0x92, 2, 2),
    RES_2_E(Kind.PREFIXED, Family.CHG_U3_R8, 0x93, 2, 2),
    RES_2_H(Kind.PREFIXED, Family.CHG_U3_R8, 0x94, 2, 2),
    RES_2_L(Kind.PREFIXED, Family.CHG_U3_R8, 0x95, 2, 2),
    RES_2_A(Kind.PREFIXED, Family.CHG_U3_R8, 0x97, 2, 2),
    RES_3_B(Kind.PREFIXED, Family.CHG_U3_R8, 0x98, 2, 2),
    RES_3_C(Kind.PREFIXED, Family.CHG_U3_R8, 0x99, 2, 2),
    RES_3_D(Kind.PREFIXED, Family.CHG_U3_R8, 0x9A, 2, 2),
    RES_3_E(Kind.PREFIXED, Family.CHG_U3_R8, 0x9B, 2, 2),
    RES_3_H(Kind.PREFIXED, Family.CHG_U3_R8, 0x9C, 2, 2),
    RES_3_L(Kind.PREFIXED, Family.CHG_U3_R8, 0x9D, 2, 2),
    RES_3_A(Kind.PREFIXED, Family.CHG_U3_R8, 0x9F, 2, 2),
    RES_4_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xA0, 2, 2),
    RES_4_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xA1, 2, 2),
    RES_4_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xA2, 2, 2),
    RES_4_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xA3, 2, 2),
    RES_4_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xA4, 2, 2),
    RES_4_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xA5, 2, 2),
    RES_4_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xA7, 2, 2),
    RES_5_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xA8, 2, 2),
    RES_5_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xA9, 2, 2),
    RES_5_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xAA, 2, 2),
    RES_5_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xAB, 2, 2),
    RES_5_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xAC, 2, 2),
    RES_5_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xAD, 2, 2),
    RES_5_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xAF, 2, 2),
    RES_6_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xB0, 2, 2),
    RES_6_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xB1, 2, 2),
    RES_6_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xB2, 2, 2),
    RES_6_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xB3, 2, 2),
    RES_6_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xB4, 2, 2),
    RES_6_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xB5, 2, 2),
    RES_6_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xB7, 2, 2),
    RES_7_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xB8, 2, 2),
    RES_7_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xB9, 2, 2),
    RES_7_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xBA, 2, 2),
    RES_7_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xBB, 2, 2),
    RES_7_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xBC, 2, 2),
    RES_7_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xBD, 2, 2),
    RES_7_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xBF, 2, 2),
    SET_0_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xC0, 2, 2),
    SET_0_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xC1, 2, 2),
    SET_0_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xC2, 2, 2),
    SET_0_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xC3, 2, 2),
    SET_0_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xC4, 2, 2),
    SET_0_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xC5, 2, 2),
    SET_0_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xC7, 2, 2),
    SET_1_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xC8, 2, 2),
    SET_1_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xC9, 2, 2),
    SET_1_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xCA, 2, 2),
    SET_1_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xCB, 2, 2),
    SET_1_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xCC, 2, 2),
    SET_1_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xCD, 2, 2),
    SET_1_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xCF, 2, 2),
    SET_2_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xD0, 2, 2),
    SET_2_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xD1, 2, 2),
    SET_2_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xD2, 2, 2),
    SET_2_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xD3, 2, 2),
    SET_2_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xD4, 2, 2),
    SET_2_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xD5, 2, 2),
    SET_2_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xD7, 2, 2),
    SET_3_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xD8, 2, 2),
    SET_3_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xD9, 2, 2),
    SET_3_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xDA, 2, 2),
    SET_3_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xDB, 2, 2),
    SET_3_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xDC, 2, 2),
    SET_3_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xDD, 2, 2),
    SET_3_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xDF, 2, 2),
    SET_4_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xE0, 2, 2),
    SET_4_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xE1, 2, 2),
    SET_4_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xE2, 2, 2),
    SET_4_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xE3, 2, 2),
    SET_4_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xE4, 2, 2),
    SET_4_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xE5, 2, 2),
    SET_4_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xE7, 2, 2),
    SET_5_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xE8, 2, 2),
    SET_5_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xE9, 2, 2),
    SET_5_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xEA, 2, 2),
    SET_5_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xEB, 2, 2),
    SET_5_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xEC, 2, 2),
    SET_5_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xED, 2, 2),
    SET_5_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xEF, 2, 2),
    SET_6_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xF0, 2, 2),
    SET_6_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xF1, 2, 2),
    SET_6_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xF2, 2, 2),
    SET_6_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xF3, 2, 2),
    SET_6_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xF4, 2, 2),
    SET_6_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xF5, 2, 2),
    SET_6_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xF7, 2, 2),
    SET_7_B(Kind.PREFIXED, Family.CHG_U3_R8, 0xF8, 2, 2),
    SET_7_C(Kind.PREFIXED, Family.CHG_U3_R8, 0xF9, 2, 2),
    SET_7_D(Kind.PREFIXED, Family.CHG_U3_R8, 0xFA, 2, 2),
    SET_7_E(Kind.PREFIXED, Family.CHG_U3_R8, 0xFB, 2, 2),
    SET_7_H(Kind.PREFIXED, Family.CHG_U3_R8, 0xFC, 2, 2),
    SET_7_L(Kind.PREFIXED, Family.CHG_U3_R8, 0xFD, 2, 2),
    SET_7_A(Kind.PREFIXED, Family.CHG_U3_R8, 0xFF, 2, 2),
    BIT_0_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x46, 2, 3),
    BIT_1_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x4E, 2, 3),
    BIT_2_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x56, 2, 3),
    BIT_3_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x5E, 2, 3),
    BIT_4_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x66, 2, 3),
    BIT_5_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x6E, 2, 3),
    BIT_6_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x76, 2, 3),
    BIT_7_HLR(Kind.PREFIXED, Family.BIT_U3_HLR, 0x7E, 2, 3),
    RES_0_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0x86, 2, 4),
    RES_1_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0x8E, 2, 4),
    RES_2_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0x96, 2, 4),
    RES_3_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0x9E, 2, 4),
    RES_4_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xA6, 2, 4),
    RES_5_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xAE, 2, 4),
    RES_6_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xB6, 2, 4),
    RES_7_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xBE, 2, 4),
    SET_0_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xC6, 2, 4),
    SET_1_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xCE, 2, 4),
    SET_2_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xD6, 2, 4),
    SET_3_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xDE, 2, 4),
    SET_4_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xE6, 2, 4),
    SET_5_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xEE, 2, 4),
    SET_6_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xF6, 2, 4),
    SET_7_HLR(Kind.PREFIXED, Family.CHG_U3_HLR, 0xFE, 2, 4);

    public enum Kind { DIRECT, PREFIXED }
    public enum Family {
        NOP,

        // Load
        LD_R8_HLR,
        LD_A_HLRU,
        LD_A_N8R,
        LD_A_CR,
        LD_A_N16R,
        LD_A_BCR,
        LD_A_DER,
        LD_R8_N8,
        LD_R16SP_N16,
        POP_R16,

        // Store
        LD_HLR_R8,
        LD_HLRU_A,
        LD_N8R_A,
        LD_CR_A,
        LD_N16R_A,
        LD_BCR_A,
        LD_DER_A,
        LD_HLR_N8,
        LD_N16R_SP,
        PUSH_R16,

        // Move
        LD_R8_R8,
        LD_SP_HL,

        // Add
        ADD_A_R8,
        ADD_A_N8,
        ADD_A_HLR,
        INC_R8,
        INC_HLR,
        INC_R16SP,
        ADD_HL_R16SP,
        LD_HLSP_S8,

        // Subtract / compare
        SUB_A_R8,
        SUB_A_N8,
        SUB_A_HLR,
        DEC_R8,
        DEC_HLR,
        CP_A_R8,
        CP_A_N8,
        CP_A_HLR,
        DEC_R16SP,

        // And
        AND_A_N8,
        AND_A_R8,
        AND_A_HLR,

        // (Inclusive) or
        OR_A_R8,
        OR_A_N8,
        OR_A_HLR,

        // Exclusive or
        XOR_A_R8,
        XOR_A_N8,
        XOR_A_HLR,

        // Rotate
        ROTCA,
        ROTA,
        ROTC_R8,
        ROT_R8,
        ROTC_HLR,
        ROT_HLR,
        SWAP_R8,
        SWAP_HLR,

        // Shift
        SLA_R8,
        SRA_R8,
        SRL_R8,
        SLA_HLR,
        SRA_HLR,
        SRL_HLR,

        // Bit test and (re)set
        BIT_U3_R8,
        BIT_U3_HLR,
        CHG_U3_R8,
        CHG_U3_HLR,

        // Miscellaneous ALU operations
        DAA,
        CPL,
        SCCF,

        // Jumps
        JP_HL,
        JP_N16,
        JP_CC_N16,
        JR_E8,
        JR_CC_E8,

        // Procedure call and return
        CALL_N16,
        CALL_CC_N16,
        RST_U3,
        RET,
        RET_CC,

        // Interrupt handling
        EDI,
        RETI,

        // Miscellaneous control instructions
        HALT,
        STOP
    }

    public final Kind kind;
    public final Family family;
    public final int encoding;
    public final int totalBytes;
    public final int cycles, additionalCycles;

    
    private Opcode(Kind kind, Family family, int encoding, int totalBytes, int cycles, int additionalCycles) {
        this.kind = kind;
        this.family = family;
        this.encoding = encoding;
        this.totalBytes = totalBytes;
        this.cycles = cycles;
        this.additionalCycles = additionalCycles;
    }

    private Opcode(Kind kind, Family family, int encoding, int totalBytes, int cycles) {
        this(kind, family, encoding, totalBytes, cycles, 0);
    }
}
