package com.yarolegovich.wellsample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.wellsql.generated.StrangePairTable;
import com.wellsql.generated.SuperHeroTable;
import com.wellsql.generated.VillainTable;
import com.yarolegovich.wellsql.SelectQuery;
import com.yarolegovich.wellsql.WellCursor;
import com.yarolegovich.wellsql.WellSql;
import com.yarolegovich.wellsql.mapper.InsertMapper;
import com.yarolegovich.wellsql.mapper.SQLiteMapper;
import com.yarolegovich.wellsql.mapper.SelectMapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by yarolegovich on 27.11.2015.
 */
@RunWith(AndroidJUnit4.class)
public class WellSqlTest {

    @BeforeClass
    public static void setUpDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        WellConfig config = new WellConfig(context);
        WellSql.init(config);
        config.reset();
    }

    @Before
    public void clearDb() {
        WellSql.delete(StrangePair.class).execute();
        WellSql.delete(SuperHero.class).execute();
        WellSql.delete(Villain.class).execute();
    }

    @Test
    public void idSetAfterInsert() {
        WellSql.autoincrementFor(SuperHero.class).reset();
        SuperHero hero = getHeroes().get(0);
        WellSql.insert(hero).execute();
        assertEquals(1, hero.getId());
    }

    @Test
    public void updateByIdWorks() {
        SuperHero hero = getHeroes().get(0);
        WellSql.insert(hero).execute();
        hero.setFoughtVillains(4);
        int rowsUpdated = WellSql.update(SuperHero.class).whereId(hero.getId()).put(hero).execute();
        assertEquals(rowsUpdated, 1);
        hero = WellSql.select(SuperHero.class)
                .where().equals(SuperHeroTable.NAME, hero.getName()).endWhere()
                .getAsModel(new SelectMapper<SuperHero>() {
                    @Override
                    public SuperHero convert(Cursor cursor) {
                        SuperHero hero = new SuperHero();
                        hero.setFoughtVillains(cursor.getInt(cursor.getColumnIndex(SuperHeroTable.FOUGHT)));
                        return hero;
                    }
                }).get(0);
        assertEquals(4, hero.getFoughtVillains());
    }

    @Test
    public void replaceAllWorks() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        int rowsUpdated = WellSql.update(SuperHero.class).replaceWhereId(heroes);
        assertEquals(heroes.size(), rowsUpdated);
    }

    @Test
    public void selectAllWorks() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        List<SuperHero> stored = WellSql.select(SuperHero.class).getAsModel();
        assertEquals(heroes.size(), stored.size());
    }

    @Test
    public void limitWorks() {
        WellSql.insert(getHeroes()).asSingleTransaction(true).execute();
        List<SuperHero> heroes = WellSql.select(SuperHero.class).limit(1).getAsModel();
        assertEquals(1, heroes.size());
    }

    @Test
    public void asyncDeliversToMainThread() {
        final List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        WellSql.select(SuperHero.class).getAsModelAsync(new SelectQuery.Callback<List<SuperHero>>() {
            @Override
            public void onDataReady(List<SuperHero> data) {
                assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
                assertEquals(heroes.size(), data.size());
            }
        });
    }

    @Test
    public void conditionalDeleteWorks() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        int rowsDeleted = WellSql.delete(SuperHero.class).where()
                .equals(SuperHeroTable.NAME, heroes.get(0).getName())
                .endWhere().execute();
        assertEquals(1, rowsDeleted);
        List<SuperHero> dbHeroes = WellSql.select(SuperHero.class).getAsModel();
        assertEquals(heroes.size() - 1, dbHeroes.size());
    }

    @Test
    public void complexSelectsWork() {
        WellSql.insert(getHeroes()).execute();

        List<SuperHero> heroes = WellSql.select(SuperHero.class)
                .where().greaterThenOrEqual(SuperHeroTable.FOUGHT, 12).or()
                .beginGroup().equals(SuperHeroTable.NAME, "Groot").or()
                .equals(SuperHeroTable.NAME, "Rocket Raccoon").endGroup().endWhere()
                .orderBy(SuperHeroTable.FOUGHT, SelectQuery.ORDER_DESCENDING)
                .limit(12)
                .getAsModel();

        assertEquals(4, heroes.size());
        assertTrue(heroes.get(0).getName().equals("Douglas Adams"));
    }

    @Test
    public void multipleOrderByWorks() {
        WellSql.insert(getHeroes()).execute();

        SuperHero superHero = new SuperHero("FluxC", 1);
        superHero.setFoughtVillains(42);
        WellSql.insert(superHero).execute();

        List<SuperHero> heroes = WellSql.select(SuperHero.class)
                .where().greaterThenOrEqual(SuperHeroTable.FOUGHT, 12).or()
                .beginGroup().equals(SuperHeroTable.NAME, "Groot").or()
                .equals(SuperHeroTable.NAME, "Rocket Raccoon").endGroup().endWhere()
                .orderBy(SuperHeroTable.FOUGHT, SelectQuery.ORDER_DESCENDING)
                .orderBy(SuperHeroTable.NAME, SelectQuery.ORDER_DESCENDING)
                .limit(12)
                .getAsModel();

        assertEquals(5, heroes.size());
        assertTrue(heroes.get(0).getName().equals("FluxC"));
        assertTrue(heroes.get(1).getName().equals("Douglas Adams"));
    }

    @Test
    public void constraintsWork() {
        SuperHero hero = getHeroes().get(0);
        hero.setFoughtVillains(-1);
        WellSql.insert(hero).execute();
        assertEquals(0, WellSql.select(SuperHero.class).getAsModel().size());
    }

    @Test
    public void conditionalUpdateWorks() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        SuperHero iWantBeLikeHim = heroes.get(6);
        int rowsUpdated = WellSql.update(SuperHero.class).where()
                .equals(SuperHeroTable.NAME, iWantBeLikeHim.getName()).endWhere()
                .put("yarolegovich", new InsertMapper<String>() {
                    @Override
                    public ContentValues toCv(String item) {
                        ContentValues cv = new ContentValues();
                        cv.put(SuperHeroTable.NAME, item);
                        return cv;
                    }
                }).execute();
        assertEquals(1, rowsUpdated);
        List<SuperHero> updated = WellSql.select(SuperHero.class)
                .where().contains(SuperHeroTable.NAME, "govich").endWhere()
                .getAsModel();
        assertEquals(1, updated.size());
        assertEquals(iWantBeLikeHim.getId(), updated.get(0).getId());
    }

    @Test
    public void conditionClauseInWorks() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        List<SuperHero> found = WellSql.select(SuperHero.class)
                .where().isIn(SuperHeroTable.FOUGHT, Arrays.asList(3, 4)).endWhere()
                .getAsModel();
        assertEquals(3, found.size());
    }

    @Test
    public void conditionClauseEqualAndNotEqual() {
        List<SuperHero> heroes = getHeroes();
        WellSql.insert(heroes).execute();
        List<SuperHero> found = WellSql.select(SuperHero.class)
                .where()
                .equals(SuperHeroTable.NAME, "Douglas Adams")
                .not().equals(SuperHeroTable.FOUGHT, 12)
                .endWhere()
                .getAsModel();
        assertEquals(1, found.size());
    }

    @Test
    public void insertIdNoAutoincrementWorks() {
        WellSql.insert(getVillains()).execute();
        List<Villain> villains = WellSql.select(Villain.class).getAsModel();
        assertEquals(getVillains().get(0).getId(), villains.get(0).getId());
    }

    @Test
    public void cursorWorks() {
        WellSql.insert(getHeroes()).execute();
        WellCursor<SuperHero> heroCursor = WellSql.select(SuperHero.class).getAsCursor();
        int counter = 0;
        SuperHero hero;
        while((hero = heroCursor.next()) != null) {
            System.out.println(hero.getName());
            counter++;
        }
        assertEquals(getHeroes().size(), counter);
    }

    @Test
    public void updateIgnoreIdWorks() {
        SuperHero hero = new SuperHero("Jean", 1);
        WellSql.insert(hero).execute();

        // SuperHero touches radioactive material, his name changes and he fights 41 villains.
        SuperHero transformedHero = new SuperHero("Jeanne", 42);

        // Replace the old entry, but keep his id
        WellSql.update(SuperHero.class).whereId(hero.getId()).put(transformedHero, new InsertMapper<SuperHero>() {
            @Override
            public ContentValues toCv(SuperHero item) {
                SQLiteMapper<SuperHero> mapper = WellSql.mapperFor(SuperHero.class);
                ContentValues cv = mapper.toCv(item);
                cv.remove(SuperHeroTable.ID);
                return cv;
            }
        }).execute();

        // Make sure update worked and id is the same
        List<SuperHero> selectedHeroes = WellSql.select(SuperHero.class).getAsModel();
        assertEquals("Jeanne", selectedHeroes.get(0).getName());
        assertEquals(hero.getId(), selectedHeroes.get(0).getId());
    }

    @Test
    public void foreignKeyClauseWorks() {
        SuperHero superHero = new SuperHero("Superman", 50);
        WellSql.insert(superHero).execute();
        List<SuperHero> superHeroes = WellSql.select(SuperHero.class).getAsModel();
        assertEquals(1, superHeroes.size());

        Villain villain = new Villain(5, "Lex Luthor", 10);
        WellSql.insert(villain).execute();
        List<Villain> villains = WellSql.select(Villain.class).getAsModel();
        assertEquals(1, villains.size());

        StrangePair strangePair = new StrangePair(1, superHero.getId(), villain.getId());
        WellSql.insert(strangePair).execute();
        List<StrangePair> strangePairs = WellSql.select(StrangePair.class).getAsModel();
        assertEquals(1, strangePairs.size());

        try {
            // Shouldn't be able to delete, since an entry in StrangePair is mapped to this SuperHero
            WellSql.delete(SuperHero.class).where()
                    .equals(SuperHeroTable.ID, superHero.getId())
                    .endWhere().execute();
            Assert.fail("Expected SQLiteConstraintException: FOREIGN KEY constraint failed");
        } catch(SQLiteConstraintException e) {
            // No op
        }

        try {
            // Shouldn't be able to delete, since an entry in StrangePair is mapped to this Villain
            WellSql.delete(Villain.class).where()
                    .equals(VillainTable.ID, villain.getId())
                    .endWhere().execute();
            Assert.fail("Expected SQLiteConstraintException: FOREIGN KEY constraint failed");
        } catch(SQLiteConstraintException e) {
            // No op
        }

        // Once the StrangePair with the foreign key is removed, we should be able to delete the SuperHero and Villain
        int rowsDeleted = WellSql.delete(StrangePair.class).where()
                .equals(StrangePairTable.ID, strangePair.getId())
                .endWhere().execute();
        assertEquals(1, rowsDeleted);

        rowsDeleted = WellSql.delete(SuperHero.class).where()
                .equals(SuperHeroTable.ID, superHero.getId())
                .endWhere().execute();
        assertEquals(1, rowsDeleted);

        rowsDeleted = WellSql.delete(Villain.class).where()
                .equals(VillainTable.ID, villain.getId())
                .endWhere().execute();
        assertEquals(1, rowsDeleted);
    }

    @Test
    public void selectBooleanConditionWorks() {
        List<SuperHero> heroes = getHeroes();
        heroes.get(3).setIsTrueEvil(true);
        heroes.get(5).setIsTrueEvil(true);
        WellSql.insert(heroes).execute();
        int evilHeroes = WellSql.select(SuperHero.class)
                .where().equals(SuperHeroTable.IS_TRUE_EVIL, true).endWhere()
                .getAsCursor().getCount();
        assertEquals(2, evilHeroes);
    }

    @Test
    public void checkLongValuesInsertAndQuery() {
        List<SuperHero> heroes = getHeroes();
        heroes.get(3).setLongField(Long.MAX_VALUE);
        heroes.get(4).setLongField(Long.MAX_VALUE);
        WellSql.insert(heroes).execute();
        int evilHeroes = WellSql.select(SuperHero.class)
                .where().equals(SuperHeroTable.LONG_FIELD, Long.MAX_VALUE).endWhere()
                .getAsCursor().getCount();
        assertEquals(2, evilHeroes);
    }

    @Test
    public void checkLongValuesInsertAndGet() {
        SuperHero superHero = new SuperHero("FluxC", 1);
        superHero.setLongField(Long.MAX_VALUE);
        superHero.setLongerField(Long.MAX_VALUE);
        WellSql.insert(superHero).execute();
        SuperHero fluxC = WellSql.select(SuperHero.class).getAsModel().get(0);
        assertEquals(Long.MAX_VALUE, fluxC.getLongField());
        assertEquals(Long.MAX_VALUE, fluxC.getLongerField().longValue());
    }

    @Test
    public void checkDoubleValuesInsertAndGet() {
        SuperHero superHero = new SuperHero("FluxC", 1);
        superHero.setDoubleField(1f / 3);
        superHero.setDoublerField(Double.MAX_VALUE);
        WellSql.insert(superHero).execute();
        SuperHero fluxC = WellSql.select(SuperHero.class).getAsModel().get(0);
        assertEquals(1f / 3, fluxC.getDoubleField(), 0.00001);
        assertEquals(Double.MAX_VALUE, fluxC.getDoublerField(), 0.00001);
    }

    @Test
    public void checkFloatValuesInsertAndGet() {
        SuperHero superHero = new SuperHero("FluxC", 1);
        superHero.setFloatField(1f / 3);
        superHero.setFloaterField(Float.MAX_VALUE);
        WellSql.insert(superHero).execute();
        SuperHero fluxC = WellSql.select(SuperHero.class).getAsModel().get(0);
        assertEquals(1f / 3, fluxC.getFloatField(), 0.00001);
        assertEquals(Float.MAX_VALUE, fluxC.getFloaterField(), 0.00001);
    }

    @Test
    public void checkConcurrentInserts() throws InterruptedException {
        int N = 100;
        final CountDownLatch countDownLatch = new CountDownLatch(N);
        for (int i = 0; i < N; i++) {
            final String name = "FluxC" + String.valueOf(i);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    WellSql.insert(new SuperHero(name, 1)).execute();
                    // Random select to probe for a crash
                    WellSql.select(SuperHero.class).getAsCursor().getCount();
                    countDownLatch.countDown();
                }
            }).start();
        }
        assertNotEquals(false, countDownLatch.await(30, TimeUnit.SECONDS));
        int heroesCount = WellSql.select(SuperHero.class).getAsCursor().getCount();
        assertEquals(N, heroesCount);
    }

    @Test
    public void checkConcurrentSelects() throws InterruptedException {
        int N = 100;
        final CountDownLatch countDownLatch = new CountDownLatch(N);
        for (int i = 0; i < N; i++) {
            String name = "FluxC" + String.valueOf(i);
            WellSql.insert(new SuperHero(name, 1)).execute();
        }
        for (int i = 0; i < N; i++) {
            final String name = "FluxC" + String.valueOf(i);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int heroCount = WellSql.select(SuperHero.class).where().equals(SuperHeroTable.NAME, name).endWhere()
                                .getAsCursor().getCount();
                    assertEquals(1, heroCount);
                    countDownLatch.countDown();
                }
            }).start();
        }
        assertNotEquals(false, countDownLatch.await(30, TimeUnit.SECONDS));
    }

    private List<SuperHero> getHeroes() {
        return Arrays.asList(
                new SuperHero("Hank Pym", 1),
                new SuperHero("Hulk", 3),
                new SuperHero("Rocket Raccoon", 22),
                new SuperHero("Douglas Adams", 42),
                new SuperHero("Iron Man", 4),
                new SuperHero("Thor", 12),
                new SuperHero("Jake Wharton", 7),
                new SuperHero("Groot", 2),
                new SuperHero("Nick Fury", 4)
        );
    }

    private List<Villain> getVillains() {
        return Arrays.asList(
                new Villain(12, "Electro", 133),
                new Villain(1488, "Red Scull", 1214),
                new Villain(95, "Sandman", 241)
        );
    }
}
