package IF.Utilities.Requirement;

import IF.Utilities.Scripting.Logger;
import org.dreambot.api.methods.quest.book.FreeQuest;
import org.dreambot.api.methods.quest.book.MiniQuest;
import org.dreambot.api.methods.quest.book.PaidQuest;
import org.dreambot.api.methods.quest.book.Quest;

import java.io.Serial;

public class QuestRequirement implements IRequirement
{
    @Serial
    private static final long       serialVersionUID = 8659572019539432254L;
    private transient    Quest      quest            = null;
    private              Quest.Type QuestType        = null;
    private              String     QuestName        = null;

    public QuestRequirement(Quest quest)
    {
        this.quest = quest;
        QuestType  = quest.getType();
        QuestName  = quest.toString();
    }

    public QuestRequirement(String Name, Quest.Type type)
    {
        QuestType = type;
        QuestName = Name;
        switch(type)
        {
            case F2P ->
            {
                quest = FreeQuest.valueOf(QuestName);
            }
            case P2P ->
            {
                quest = PaidQuest.valueOf(QuestName);
            }
            case MINIQUEST ->
            {
                quest = MiniQuest.valueOf(QuestName);
            }
        }
    }

    /**
     * @return
     */
    @Override
    public RequirementType GetRequirementType()
    {
        return RequirementType.Quest;
    }

    /**
     * @return
     */
    @Override
    public boolean isRequirementMet()
    {
        var questie = GetQuest();
        return questie != null && questie.isFinished();
    }

    public Quest GetQuest()
    {
        if(quest != null)
        {
            return quest;
        }

        if(QuestType != null && QuestName != null)
        {
            switch(QuestType)
            {
                case F2P ->
                {
                    quest = FreeQuest.valueOf(QuestName);
                }
                case P2P ->
                {
                    quest = PaidQuest.valueOf(QuestName);
                }
                case MINIQUEST ->
                {
                    quest = MiniQuest.valueOf(QuestName);
                }
            }
            return quest;
        }

        Logger.log(
                "QuestRequirement: GetQuest: Quest ref, quest type and/or quest name is not defined");
        return null;
    }
}
