package org.sandholm.max.kart;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.Map;

public class Kart {
    static float FRICTION_S = 2f;

    float turningSpeed;
    float driftingSpeed;
    float engineForce;
    float width;
    float length;
    float density;

    Texture spriteSheet;
    TextureRegion[] kartSprites;

    Body pBody;

    Vector2 position;
    float rotation;

    public Kart(String kartName, Vector2 position, float rotation, World world) {
        try {
            JSONParser parser = new JSONParser();
            Map obj = (Map)parser.parse(new FileReader("karts/"+kartName+".json"));
            turningSpeed = ((Number)obj.get("turningSpeed")).floatValue();
            driftingSpeed = ((Number)obj.get("driftingSpeed")).floatValue();
            engineForce = ((Number)obj.get("engineForce")).floatValue();
            width = ((Number)obj.get("width")).floatValue();
            length = ((Number)obj.get("length")).floatValue();
            density = ((Number)obj.get("density")).floatValue();
            spriteSheet = new Texture("karts/"+obj.get("sprites"));
            kartSprites = new TextureRegion[22];
            for (int i=0; i<=11; i++){  //make TextureRegions from the normal sprites
                kartSprites[i] = new TextureRegion(spriteSheet, i*32, 0, 32, 32);
            }
            for (int i=12; i<=21; i++){ //make TextureRegions from the sprites flipped to make the complete circle
                kartSprites[i] = new TextureRegion(spriteSheet, (22-i)*32+32, 0, -32, 32);
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

        this.position = position;
        this.rotation = rotation;

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(this.position);
        bodyDef.angle = rotation*MathUtils.degreesToRadians;
        bodyDef.fixedRotation = true;
        pBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(length, width);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = density;
        fixtureDef.restitution = 1f;
        pBody.setLinearDamping(FRICTION_S);
        pBody.createFixture(fixtureDef);
        shape.dispose();
    }

    public void update(float deltaTime) {
        position.set(pBody.getPosition());
        pBody.setTransform(pBody.getPosition(), rotation*MathUtils.degreesToRadians);
    }

    public void resetFrame() {
        pBody.setLinearDamping(FRICTION_S);
    }

    public Vector2 getPosition() {
        return position;
    }

    public float getRotation() {
        return rotation;
    }

    public TextureRegion getTextureRegionFromAngle(float angle) {
        return kartSprites[(((int)Math.floor((angle+360/(kartSprites.length*2))/(360/kartSprites.length))) % kartSprites.length + kartSprites.length ) % kartSprites.length];
    }

    public void accelerate() {
        pBody.applyForceToCenter(new Vector2(engineForce, 0).rotate(rotation), true);
    }

    public void stopAccelerating() { //useless
    }

    public void brake() {
        pBody.applyForceToCenter(pBody.getLinearVelocity().cpy().rotate(180).scl(1.5f).add(new Vector2(-35f, 0).rotate(rotation)), true);
    }

    public void stopBraking() { //useless
    }

    public void turn(float amount, float deltaTime, boolean drift) { //amount defines how much to turn, and what direction, float between -1 and 1

        if (!drift) {
            rotation += turningSpeed*amount*deltaTime;
            rotation = (rotation % 360 + 360) % 360;
        } else {
            rotation += driftingSpeed*amount*deltaTime;
            rotation = (rotation % 360 + 360) % 360;
            pBody.applyForceToCenter(new Vector2(2f*amount, 0f).rotate(rotation+90), true);
            pBody.setLinearDamping(FRICTION_S-(1f*Math.abs(amount)));
        }
    }




}
