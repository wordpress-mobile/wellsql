package com.yarolegovich.wellsample;

import com.yarolegovich.wellsql.core.Identifiable;
import com.yarolegovich.wellsql.core.annotation.Column;
import com.yarolegovich.wellsql.core.annotation.PrimaryKey;
import com.yarolegovich.wellsql.core.annotation.RawConstraints;
import com.yarolegovich.wellsql.core.annotation.Table;
import com.yarolegovich.wellsql.core.annotation.Unique;

@Table
@RawConstraints({"FOREIGN KEY(HERO_ID) REFERENCES SuperHero(_id)", "FOREIGN KEY(VILLAIN_ID) REFERENCES Villain(_id)"})
public class AntiHero implements Identifiable {
    @Column
    @PrimaryKey(autoincrement = false)
    private int id;
    @Column
    private int heroId;
    @Column
    private int villainId;
    @Column @Unique
    private String name;

    public AntiHero() { }

    public AntiHero(int id, String name, int heroId, int villainId) {
        this.id = id;
        this.name = name;
        this.heroId = heroId;
        this.villainId = villainId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }

    public int getVillainId() {
        return villainId;
    }

    public void setVillainId(int villainId) {
        this.villainId = villainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
