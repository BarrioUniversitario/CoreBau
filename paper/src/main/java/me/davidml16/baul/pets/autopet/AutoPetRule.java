package me.davidml16.baul.pets.autopet;

import me.davidml16.baul.pets.pet.Pet;
import me.davidml16.baul.pets.utils.Messages;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class AutoPetRule {

    private final UUID ruleId;
    private final TriggerType trigger;
    private final Pet selectedPet;
    private final Set<AutoPetException> exceptions;
    private final ItemStack icon;

    public AutoPetRule(TriggerType trigger, Pet selectedPet) {
        this.ruleId = UUID.randomUUID();
        this.trigger = trigger;
        this.selectedPet = selectedPet;
        this.exceptions = new HashSet<>();
        this.icon = new ItemStack(trigger.getTriggerIcon());
        icon.editMeta(e -> {
            e.displayName(Messages.deserialize(trigger.getId()));
            e.lore(Messages.deserialize("", "<green>When",
                    trigger.getDescription(), "",
                    "<green>Equip: <green>" + Messages.serialize(Messages.getPetName(selectedPet))));
            e.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        });
    }

    public UUID getRuleId() {
        return ruleId;
    }

    public TriggerType getTrigger() {
        return trigger;
    }

    public ItemStack getIcon() {

        return icon;
    }

    public Pet getSelectedPet() {
        return selectedPet;
    }

    public Set<AutoPetException> getExceptions() {
        return new HashSet<>(exceptions);
    }

    public void addException(AutoPetException exception) {
        this.exceptions.add(exception);
    }

    public void removeException(AutoPetException exception) {
        this.exceptions.remove(exception);
    }

    public boolean hasException(AutoPetException exception) {
        return this.exceptions.contains(exception);
    }

    public Map<String, Object> serialize() {
        Map<String, Object> map = new HashMap<>();
        map.put("trigger", trigger.name());
        map.put("selectedPet", selectedPet.serialize());
        List<String> exceptionList = new ArrayList<>();
        for (AutoPetException exception : exceptions) {
            exceptionList.add(exception.name());
        }
        map.put("exceptions", exceptionList);
        return map;
    }

    @SuppressWarnings("unchecked")
    public static AutoPetRule deserialize(Map<String, Object> map) {
        TriggerType trigger = TriggerType.valueOf((String) map.get("trigger"));
        Map<String, Object> petData = (Map<String, Object>) map.get("selectedPet");
        Pet selectedPet = Pet.deserialize(petData);
        AutoPetRule rule = new AutoPetRule(trigger, selectedPet);

        List<String> exceptionList = (List<String>) map.get("exceptions");
        for (String exceptionName : exceptionList) {
            rule.addException(AutoPetException.valueOf(exceptionName));
        }

        return rule;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AutoPetRule that = (AutoPetRule) obj;
        return ruleId.equals(that.ruleId);
    }

    @Override
    public int hashCode() {
        return ruleId.hashCode();
    }
}
