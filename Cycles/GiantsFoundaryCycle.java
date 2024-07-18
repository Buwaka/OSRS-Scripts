package Cycles;

//@JsonTypeName("GiantsFoundaryCycle")
public class GiantsFoundaryCycle
{
    // Tips widget IDs: 718, 9 , 102
    // (0 = new entry,
    // 2 = name of part,
    // 7 = NFH name 1,
    // 8 = NFH value 1,
    // 11 = NFH name 2,
    // 12 NFH value 2,
    // 15 NFH name 3,
    // 16 NFH value 3)

    // Success bar widget = 754, 45 check size
    // Success bar pointer widget = 754, 7 check size
    // temperature bar widget = 754, 71 check size
    // temperature bar pointer widget = 754, 74 check size
    // temperature bar widget = 754, 71 check size
    // temperature bar pointer widget = 754, 78 check size

    // Action Bar widget = 754, 75, 0-10 (10 = Action texture, 4442 = red/hammer, 4443 = yellow/belt, 4444 = green/polish


    final int MoldJigReadyID = 44777;

    final int LavaPoolID  = 44631;
    final int WaterfallID = 44632;

    final int HammerToolID = 44619; // cools dowmn
    final int GrindToolID  = 44620; // heats up
    final int PolishToolID = 44621; // cools down


    final int Max    = 823;// - 387 = 436 = 100%
    final int Min    = 387;// - 387 = 0 = 0%
    final int HotMax = 816;// - 387 = 429 = 98%
    final int HotMin = 682;// - 387 = 295 = 68%

    final int MildMax = 670;// - 387 = 283 = 65%
    final int MildMin = 534;// - 387 = 147 = 33%

    final int ColdMax = 523;// - 387 = 136 = 31%
    final int ColdMin = 388;// - 387 = 1 = 0%


}
