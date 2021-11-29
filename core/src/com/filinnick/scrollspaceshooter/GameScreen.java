package com.filinnick.scrollspaceshooter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.LinkedList;
import java.util.ListIterator;

public class GameScreen implements Screen {

    //screen
    private Camera camera;
    private Viewport viewport;

    //graphics
    private SpriteBatch batch;
    private TextureAtlas textureAtlas;

    private TextureRegion[] backgrounds;
    private float backgroundHeight; //height of background in World units

    private TextureRegion playerShipTextureRegion, playerShieldTextureRegion,
            enemyShipTextureRegion, enemyShieldTextureRegion,
            playerLaserTextureRegion, enemyLaserTextureRegion;


    //timing
    private float[] backgroundOffsets = {0,0,0,0};
    private float backgroundMaxScrollingSpeed;

    //world parameters
    private final int WORLD_WIDTH=72;
    private final int WORLD_HEIGHT=128;

    //game objects
    private Ship playerShip;
    private Ship enemyShip;
    private LinkedList<Laser> playerLaserList;
    private LinkedList<Laser> enemyLaserList;


    GameScreen() {

        camera = new OrthographicCamera();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);

        //setup texture atlas
        textureAtlas = new TextureAtlas("images.atlas");

        backgrounds = new TextureRegion[4];
        backgrounds[0] = textureAtlas.findRegion("background");
        backgrounds[1] = textureAtlas.findRegion("Starscape01");
        backgrounds[2] = textureAtlas.findRegion("Starscape02");
        backgrounds[3] = textureAtlas.findRegion("Starscape03");

        backgroundMaxScrollingSpeed = (float) (WORLD_HEIGHT)/4;

        //init texture regions
        playerShipTextureRegion = textureAtlas.findRegion("playerShip2_blue");
        enemyShipTextureRegion = textureAtlas.findRegion("enemyRed3");
        playerShieldTextureRegion = textureAtlas.findRegion("shield3");
        enemyShieldTextureRegion = textureAtlas.findRegion("shield1");

        enemyShieldTextureRegion.flip(false,true);

        playerLaserTextureRegion = textureAtlas.findRegion("laserBlue03");
        enemyLaserTextureRegion = textureAtlas.findRegion("laserRed03");

        //set up the game objects
        playerShip = new PlayerShip(48,3,WORLD_WIDTH / 2,WORLD_HEIGHT / 4,
                10,10,0.4f,4,45,0.5f,
                playerShipTextureRegion, playerShieldTextureRegion, playerLaserTextureRegion);

        enemyShip = new EnemyShip(2,1,WORLD_WIDTH / 2,WORLD_HEIGHT * 3 / 4,
                10,10,0.3f,5,50,0.8f,
                enemyShipTextureRegion, enemyShieldTextureRegion, enemyLaserTextureRegion);

        playerLaserList = new LinkedList<>();
        enemyLaserList = new LinkedList<>();

        batch = new SpriteBatch();

    }

    @Override
    public void render(float deltaTime) {
        batch.begin();

        detectInput(deltaTime);

        playerShip.update(deltaTime);
        enemyShip.update(deltaTime);

        //scrolling background
        renderBackground(deltaTime);

        //enemy ships
        enemyShip.draw(batch);

        //player ship
        playerShip.draw(batch);

        //lasers
        renderLaser(deltaTime);

        //detect collisions between lasers and ships
        detectCollisions();

        //explosions
        renderExplosions(deltaTime);

        batch.end();
    }

    private void detectInput(float deltaTime) {
        //keyboard input

        //strategy: determine the max distance th ship can move
        //check each key that matter and move accordingly

        float leftLimit, rightLimit, upLimit, downLimit;
        leftLimit = -playerShip.boundingBox.x;
        downLimit = -playerShip.boundingBox.y;
        rightLimit = WORLD_WIDTH - playerShip.boundingBox.x - playerShip.boundingBox.width;
        upLimit = WORLD_HEIGHT/2 - playerShip.boundingBox.y - playerShip.boundingBox.height;

        //TODO переменные под кнопки для пользовательского назначения кнопок
        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT) && rightLimit > 0) {
            playerShip.translate(Math.min(playerShip.movementSpeed*deltaTime, rightLimit), 0f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.UP) && upLimit > 0) {
            playerShip.translate(0f, Math.min(playerShip.movementSpeed*deltaTime, upLimit));
        }
        if(Gdx.input.isKeyPressed(Input.Keys.LEFT) && leftLimit < 0) {
            playerShip.translate(Math.max(-playerShip.movementSpeed*deltaTime, leftLimit), 0f);
        }
        if(Gdx.input.isKeyPressed(Input.Keys.DOWN) && downLimit < 0) {
            playerShip.translate(0f, Math.max(-playerShip.movementSpeed*deltaTime, downLimit));
        }

        //touch input (also mouse)
    }

    private void detectCollisions() {
        //for each player laser check intersects an enemy ship
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            if(enemyShip.intersects(laser.boundingBox)) { //hitting enemy ship
                enemyShip.hit(laser);
                iterator.remove();
            }
        }

        //for each enemy laser check intersects a player ship
        iterator = enemyLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            if(playerShip.intersects(laser.boundingBox)) { //hitting player ship
                playerShip.hit(laser);
                iterator.remove();
            }
        }

    }

    private void renderExplosions(float deltaTime) {

    }

    private void renderLaser(float deltaTime) {
        //create new lasers
        //player lasers
        if(playerShip.canFireLaser()) {
            Laser[] lasers = playerShip.fireLasers();;
            for(Laser laser:lasers) {
                playerLaserList.add(laser);
            }
        }

        //enemy lasers
        if(enemyShip.canFireLaser()) {
            Laser[] lasers = enemyShip.fireLasers();;
            for(Laser laser:lasers) {
                enemyLaserList.add(laser);
            }
        }

        //draw lasers
        //remove old lasers
        ListIterator<Laser> iterator = playerLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y += laser.movementSpeed*deltaTime;
            if(laser.boundingBox.y > WORLD_HEIGHT) {
                iterator.remove();
            }
        }
        iterator = enemyLaserList.listIterator();
        while(iterator.hasNext()) {
            Laser laser = iterator.next();
            laser.draw(batch);
            laser.boundingBox.y -= laser.movementSpeed*deltaTime;
            if(laser.boundingBox.y + laser.boundingBox.height < 0) {
                iterator.remove();
            }
        }
    }

    private void renderBackground(float deltaTime){
        backgroundOffsets[0] += deltaTime * backgroundMaxScrollingSpeed / 8;
        backgroundOffsets[1] += deltaTime * backgroundMaxScrollingSpeed / 4;
        backgroundOffsets[2] += deltaTime * backgroundMaxScrollingSpeed / 2;
        backgroundOffsets[3] += deltaTime * backgroundMaxScrollingSpeed;

        for(int layer = 0; layer < backgroundOffsets.length; layer++) {
            if(backgroundOffsets[layer] > WORLD_HEIGHT) {
                backgroundOffsets[layer] = 0;
            }
            batch.draw(backgrounds[layer],
                    0,
                    -backgroundOffsets[layer],
                    WORLD_WIDTH, WORLD_HEIGHT);
            batch.draw(backgrounds[layer],
                    0,
                    -backgroundOffsets[layer] + WORLD_HEIGHT,
                    WORLD_WIDTH, WORLD_HEIGHT);
        }

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        batch.setProjectionMatrix(camera.combined);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void show() {

    }

    @Override
    public void dispose() {

    }
}
