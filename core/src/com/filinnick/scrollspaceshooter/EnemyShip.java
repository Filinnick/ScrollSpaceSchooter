package com.filinnick.scrollspaceshooter;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EnemyShip extends Ship{


    public EnemyShip(float movementSpeed, int shield,
                      float xCentre, float yCentre,
                      float width, float height,
                      float laserWidth, float laserHeight, float laserMovementSpeed,
                      float timeBetweenShots,
                      TextureRegion shipTextureRegion,
                      TextureRegion shieldTextureRegion,
                      TextureRegion laserTextureRegion) {
        super(movementSpeed, shield, xCentre, yCentre, width, height, laserWidth, laserHeight, laserMovementSpeed, timeBetweenShots, shipTextureRegion, shieldTextureRegion, laserTextureRegion);
    }

    @Override
    public Laser[] fireLasers() {
        Laser[] lasers = new Laser[2];
        lasers[0] = new Laser(boundingBox.x + boundingBox.width*0.18f,boundingBox.y - laserHeight,
                laserWidth,laserHeight,laserMovementSpeed,laserTextureRegion);
        lasers[1] = new Laser(boundingBox.x + boundingBox.width*0.82f,boundingBox.y - laserHeight,
                laserWidth,laserHeight,laserMovementSpeed,laserTextureRegion);

        timeSinceLastShot = 0;

        return lasers;
    }

    @Override
    public void draw(Batch batch) {
        batch.draw(shipTextureRegion, boundingBox.x, boundingBox.y, boundingBox.width, boundingBox.height);
        if(shield > 0 ) {
            batch.draw(shieldTextureRegion, boundingBox.x, boundingBox.y - boundingBox.height*0.2f, boundingBox.width, boundingBox.height);
        }
    }
}
