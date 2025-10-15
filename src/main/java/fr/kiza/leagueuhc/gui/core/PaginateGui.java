package fr.kiza.leagueuhc.gui.core;

import fr.kiza.leagueuhc.LeagueUHC;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public abstract class PaginateGui extends AbstractGui {

    protected int page = 0, contentStart = 0, contentEnd = 53;

    protected final List<ItemStack> elements = new ArrayList<>();

    public PaginateGui(LeagueUHC instance, UUID viewerId) {
        super(instance, viewerId);
        this.computeContentRange();
    }

    public PaginateGui(LeagueUHC instance) {
        super(instance);
        this.computeContentRange();
    }

    private void computeContentRange() {
        int rows = this.size / 9;
        if (rows < 2) rows = 2;
        this.contentStart = 0;
        this.contentEnd = (rows - 1) * 9 - 1;
    }

    @Override
    protected void createInventory() {
        this.computeContentRange();
        super.createInventory();
        this.renderControls();
    }

    protected void renderPage(){
        if (this.inventory==null) createInventory();
        int slotsPerPage = this.contentEnd - this.contentStart +1;
        int from = this.page * slotsPerPage;
        int to = Math.min(from + slotsPerPage, elements.size());
        for(int i= this.contentStart; i <= this.contentEnd; i++) this.inventory.setItem(i, null);
        int idx = this.contentStart;
        for(int i= from; i<to; i++){
            this.inventory.setItem(idx++, this.elements.get(i));
        }
        this.renderControls();
    }

    protected void renderControls(){
        int rows = this.size / 9;
        int base = (rows - 1) * 9;

        final GuiButton previous = new GuiButton(createControlItem("§aPrevious"), (p,inv)-> {
            if(this.page > 0) this.page--; renderPage();
        });

        final GuiButton next = new GuiButton(createControlItem("§aNext"), (p,inv)-> {
            if((this.page + 1) * ((this.contentEnd - this.contentStart) + 1) < this.elements.size()) page++; renderPage();
        });

        final GuiButton indicator = new GuiButton(createControlItem("§ePage "+(page+1)+"/"+Math.max(1, (int)Math.ceil((double)elements.size()/((contentEnd-contentStart)+1)))), (p,inv)->{} , true);
        this.setButton(base + 3, previous);
        this.setButton(base + 4, indicator);
        this.setButton(base + 5, next);
    }

    protected ItemStack createControlItem(final String name){
        final ItemStack paper = new ItemStack(Material.PAPER);
        final ItemMeta meta = paper.getItemMeta();
        meta.setDisplayName(name);
        paper.setItemMeta(meta);
        return paper;
    }

    public void addElement(final ItemStack item){
        this.elements.add(item);
    }

    public void addElements(Collection<ItemStack> items){
        elements.addAll(items);
    }

    public void clearElements(){
        elements.clear(); page = 0;
    }

    public void open(Player player){
        renderPage();
        super.open(player);
    }
}