package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class MyGdxGame extends Game {
	public static final double CONST_G = -6.67 * Math.pow(10, 1);
	public static final float PPM = 32;

	private boolean DEBUG = true;

	private OrthographicCamera camera;

	private Box2DDebugRenderer b2dr;
	private World world;
//	private CelestialBody player;
	private CelestialBody planet;

	private SpriteBatch batch;
	private BitmapFont font;

	private int screenWidth, screenHeight;

	private ShapeRenderer shapeRenderer;
	private Player player;


    public MyGdxGame(int screenWidth, int screenHeight){
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont();
		shapeRenderer = new ShapeRenderer();

		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, w/2, h/2);
		Vector3 position = camera.position;
		position.x = 0;
		position.y =  100;
		camera.position.set(position);

		world = new World(new Vector2(0, 0f), false);
		b2dr = new Box2DDebugRenderer();

		planet = new CelestialBody(world, 0, 100, 64, false);
		player = new Player(shapeRenderer, new Vector2(0, 200), batch, new CelestialBody(world, 0, 200, 32, false));

		player.orbit(planet);
	}

	@Override
	public void render() {
		super.render();
		update(Gdx.graphics.getDeltaTime());

		Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (DEBUG) {
			b2dr.render(world, camera.combined.cpy().scl(PPM));
		}

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setProjectionMatrix(camera.combined.cpy().scl(1f));

        player.update();
        player.render();

        shapeRenderer.end();

        if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
	}

	@Override
	public void dispose() {
		world.dispose();
		b2dr.dispose();
		batch.dispose();
		font.dispose();
//		player.dispose();
		shapeRenderer.dispose();
	}

	public void update(float delta) {
		world.step(delta, 6, 2);

		double dX = player.getBody().getPosition().x - planet.getBody().getPosition().x;
		double dY = player.getBody().getPosition().y - planet.getBody().getPosition().y;

		double distanceSquared = Math.abs(
				Math.pow(dX, 2)
			  + Math.pow(dY, 2)
		);

		double planetMass = planet.getBody().getMass();
		double playerMass = player.getBody().getMass();

		double force = CONST_G * planetMass * playerMass
					 / distanceSquared;

		double forceX = (force / Math.sqrt(distanceSquared)) * dX;
        double forceY = (force / Math.sqrt(distanceSquared)) * dY;


        player.getBody().applyForceToCenter((float)forceX, (float)forceY, true);

		camera.update();
	}

	public void inputUpdate(float delta) {
		int horizontalForce = 0;

		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			horizontalForce -= 1;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			horizontalForce += 1;
		}

		if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
			player.getBody().applyForceToCenter(0, 300, false);
		}

		player.getBody().setLinearVelocity(horizontalForce * 5, player.getBody().getLinearVelocity().y);
	}

	public void cameraUpdate() {
		Vector3 position = camera.position;
		position.x = player.getBody().getPosition().x * PPM;
		position.y = player.getBody().getPosition().y * PPM;
		camera.position.set(position);

		camera.update();
	}
}
