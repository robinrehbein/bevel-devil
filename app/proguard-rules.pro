# Progress is saved under keys built from enum constant names ("card_" + Card.name), so they must never be renamed.
-keepclassmembers enum com.robinrehbein.beveldevil.game.Card {
    <fields>;
}
