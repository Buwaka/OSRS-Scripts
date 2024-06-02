package Cycles.SimpleTasks.Combat;

import Utilities.Scripting.SimpleTask;
import org.dreambot.api.methods.prayer.Prayer;
import org.dreambot.api.methods.prayer.Prayers;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;

import javax.annotation.Nonnull;

public class RestorePrayerTask extends SimpleTask
{
    public RestorePrayerTask(String Name)
    {
        super(Name);
    }


    public Prayer GetChurch()
    {
        return null;
    }

    /**
     * @return
     */
    @Override
    protected boolean Ready()
    {
        boolean NeedJesus = Skills.getBoostedLevel(Skill.PRAYER) < Skills.getRealLevel(Skill.PRAYER);
        return NeedJesus && super.Ready();
    }

    /**
     * @return
     */
    @Override
    protected int Loop()
    {
        return super.Loop();
    }

    @Nonnull
    @Override
    public TaskType GetTaskType()
    {
        return TaskType.RestorePrayer;
    }
}
