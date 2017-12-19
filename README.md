# WellSql
Android API for working with SQLiteDatabase can be characterized as inconvenient and ugly (at least for me). This library tends to make work with it easier and reduce amount of boilerplate code you perhaps used to write.

## Features
* Just a wrapper for old trusted methods query, insert, delete etc.
* Generates classes with public static final strings for each column name.
* Generates mappers for your model (Conversion to ContentValues and from Cursor).
* In 98% of use cases library takes care of opening/closing db and cursors.
* Just one reflexive call, nothing critical

## Add to your project

In build.gradle for your module add:

```gradle
dependencies {
    compile 'org.wordpress:wellsql:1.2.0'
    annotationProcessor 'org.wordpress:wellsql-processor:1.2.0'
}
```

### Jitpack

You can also use Jitpack:

In build.gradle for your project add:

```gradle
allprojects {
    repositories {
        ...
        maven { url "https://jitpack.io" }
    }
}
```

In build.gradle for your module add:

```gradle
dependencies {
    compile 'com.github.wordpress-mobile.wellsql:wellsql:[commit hash or branch snaphost]'
    annotationProcessor 'com.github.wordpress-mobile.wellsql:well-processor:[commit hash or branch snaphost]'
}
```

### Publish the aar to bintray

```shell
$ ./gradlew assemble publishToMavenLocal bintrayUpload -PbintrayUser=FIXME -PbintrayKey=FIXME -PdryRun=false
```

---

# Documentation

## Table creation and setup

One of the most important features to ease your life is boilerplate code generation. Here are the steps to create a table.

Create your model class, make it implement Identifiable (just two methods - getId() and setId(int id) and configure it with the help of annotations.
```java
@Table
@RawConstraints({"UNIQUE (NAME, fought)"})
public class SuperHero implements Identifiable {

    @Column
    @PrimaryKey
    private int mId;

    @Column @Unique
    private String mName;

    @Column(name = "fought")
    @Check("fought >= 0")
    private int mFoughtVillains;
}
```
Here is some weird example to show annotations you have in your arsenal. **Class also needs getters and setters, so generated mapper can work**.

Rebuild your project. Two classes SuperHeroTable and SuperHeroMapper will be generated. You don't need to do anything with the second one, but the first will contain useful fields:
```java
public final class SuperHeroTable implements TableClass {
  public static final String ID = "_id";

  public static final String NAME = "NAME";

  public static final String FOUGHT = "fought";

  …
}
```
**I strongly recommend you to have field id or mId of type int in your model class, or better write custom mappers.**

Last step is to create config for WellSql. You will need it to call
```java
WellSql.init(new MyWellConfig(context));
```

On application startup (extend application class and call this method in overriden onCreate). You can either implement interface WellConfig or extend class DefaultWellConfig (I advice to do the latter, because this class take care of binding with generated classes). Here is an example of how to extend it:

```java
public class WellConfig extends DefaultWellConfig {

    public WellConfig(Context context) {
        super(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db, WellTableManager helper) {
        helper.createTable(SuperHero.class);
        helper.createTable(Villain.class);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, WellTableManager helper, int newVersion, int oldVersion) {
        helper.dropTable(SuperHero.class);
        helper.dropTable(Villain.class);
        onCreate(db, helper);
    }

    @Override
    protected Map<Class<?>, SQLiteMapper<?>> registerMappers() {
        return super.registerMappers();
    }

    …
}
```
Method registerMethods() can be used to register your custom mappers for classes. If class has generated mapper - it will be ignored, if you pass your own implementation.
If you don't need classes to be generated #confused, you can use

```java
@Table(generateMapper = false, generateTable = false)
public class Villain implements Identifiable {
  …
}
```

## Usage examples

Builder for queries. You can get results as Map<String, Object>, where keys are column names and values are extracted from cursor. Or results can be converted to model (with the help of automatically generated mappers or your custom mappers.

You can also perform async queries. Result will be delivered to Callback object you provide on the main thread.

```java
/*
 * Asynchronously select all from SuperHeroTable
 */
WellSql.select(SuperHero.class).getAsModelAsync(new SelectQuery.Callback<List<SuperHero>>() {
    @Override
    public void onDataReady(List<SuperHero> data) {
      assertTrue(Thread.currentThread() == Looper.getMainLooper().getThread());
    }
});

/*
 * Some pointless query to show query builder
 */
List<SuperHero> heroes = WellSql.select(SuperHero.class)
    .where().greaterThen(SuperHeroTable.FOUGHT, 12)
    .beginGroup().equals(SuperHeroTable.NAME, "Groot").or()
    .equals(SuperHeroTable.NAME, "Rocket Raccoon").endGroup().endWhere()
    .orderBy(SelectQuery.ORDER_DESCENDING, SuperHeroTable.FOUGHT)
    .limit(12)
    .getAsModel();
```
The same way you can peform insert, update and delete queries.

```java
WellSql.insert(getHeroes()).asSingleTransaction(true).execute();

WellSql.delete(SuperHero.class).execute();

WellSql.delete(SuperHero.class).where()
    .greaterThenOrEqual(SuperHeroTable.FOUGHT, 12)
    .endWhere().execute();

WellSql.update(SuperHero.class).whereId(hero.getId()).put(anotherHero).execute();
```

Update only one field of a Model:

```java
WellSql.update(SuperHero.class).where()
       .equals(SuperHeroTable.NAME, "Rocket Raccoon").endWhere()
       .put("Yaroslav", new InsertMapper<String>() {
           @Override
           public ContentValues toCv(String item) {
               ContentValues cv = new ContentValues();
               cv.put(SuperHeroTable.NAME, item);
               return cv;
           }
       }).execute();
```

Factory methods of WellSql class covers most use cases (I think so), but if you want to make something unusual with db you can always call

```java
SQLiteDatabase db = WellSql.giveMeReadableDb();

SQLiteDatabase db = WellSql.giveMeWritableDb();
```
For more usage examples you can see tests of well-sample, but I think nothing extraordinary in api :)

## Licence

MIT licence
