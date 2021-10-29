package de.mediathekview.mserver.base;

/** A enumeration which represents the categories A-Z for Sender like ARD. */
public enum CategoriesAZ {
  NUMBERS("0-9"),
  LETTER_A("A"),
  LETTER_B("B"),
  LETTER_C("C"),
  LETTER_D("D"),
  LETTER_E("E"),
  LETTER_F("F"),
  LETTER_G("G"),
  LETTER_H("H"),
  LETTER_I("I"),
  LETTER_J("J"),
  LETTER_K("K"),
  LETTER_L("L"),
  LETTER_M("M"),
  LETTER_N("N"),
  LETTER_O("O"),
  LETTER_P("P"),
  LETTER_Q("Q"),
  LETTER_R("R"),
  LETTER_S("S"),
  LETTER_T("T"),
  LETTER_U("U"),
  LETTER_V("V"),
  LETTER_W("W"),
  LETTER_X("X"),
  LETTER_Y("Y"),
  LETTER_Z("Z");

  private final String key;

  CategoriesAZ(final String aKey) {
    key = aKey;
  }

  public String getKey() {
    return key;
  }
}
