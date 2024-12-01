package IF.Logic.Cycles.Minigame;

import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.widgets.WidgetChild;

public class GOTRCycle
{
    final Area   StartArea       = new Area(3617, 9482, 3611, 9471);
    final int    BarrierClosedID = 43849;
    final int    BarrierOpenID   = 43700;
    final int    WorkBenchID     = 43754;
    final int    RuneDepositID   = 43696;
    final int    PortalID        = 43729; ///
    final int    PortalExitID    = 38044;
    final int    AgilityLevel    = 56;
    final String MineAction      = "Mine";

    //items
    final int GuardianFragmentsID = 26878;
    final int GuardianEssenceID   = 26879;

    //Altars
    final int EarthAltarPortalID = 43703;
    final int EarthAltarID       = 34763;
    final int WindAltarPortalID  = 43701;
    final int WindAltarID        = 34760;
    final int MindAltarPortalID  = 43705;
    final int MindAltarID        = 34761;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;
    //    final int EarthAltarPortalID = 43703;
    //    final int EarthAltarID = 34763;

    private int GetGuardianProgress()
    {
        final WidgetChild FullBarWidget     = Widgets.get(746, 7);
        final WidgetChild ProgressBarWidget = Widgets.get(746, 8);
        if(FullBarWidget == null || ProgressBarWidget == null)
        {
            return 0;
        }

        final float FullBarSize     = FullBarWidget.getWidth();
        final float ProgressBarSize = ProgressBarWidget.getWidth();
        return (int) (ProgressBarSize / FullBarSize * 100);
    }

    private GameObject GetGuardianRemainsMine()
    {
        return GameObjects.closest(t -> t.hasAction(MineAction));
    }

    private boolean isGuardianDown()
    {
        final int Idle = -1;
        final int Down = 9374;
        return false;
    }


}
