package com.seedfinding.minemap.util.snksynthesis.voxelgame;

import com.seedfinding.minemap.util.snksynthesis.voxelgame.block.Block;
import com.seedfinding.minemap.util.snksynthesis.voxelgame.block.BlockManager;
import com.seedfinding.minemap.util.snksynthesis.voxelgame.block.BlockType;
import com.seedfinding.minemap.util.snksynthesis.voxelgame.gfx.Font;
import com.seedfinding.minemap.util.snksynthesis.voxelgame.gfx.Shader;
import com.seedfinding.minemap.util.snksynthesis.voxelgame.gfx.Window;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL33.*;

public class Visualizer {
    public static final String PATH = "/others";
    private Window window;
    private Camera cam;
    private Shader shader;
    private Shader lightShader;
    private final BlockManager blockManager;
    private Block light;
    private Vector3f lightPos;
    private Font font;
    private String text = null;
    protected static final EventManager eventManager = new EventManager();
    private boolean isRunning = false;

    private void draw(MemoryStack stack) {
        // Bind shader
        shader.bind();

        // Set light color and position
        glUniform3fv(shader.getLocation("lightPos"), lightPos.get(stack.mallocFloat(3)));
        glUniform3f(shader.getLocation("lightColor"), 1.0f, 1.0f, 1.0f);

        // Projection Matrix
        float aspectRatio = (float) window.getWidth() / window.getHeight();
        Matrix4f projection = new Matrix4f().setPerspective((float) Math.toRadians(100.0f), aspectRatio, 0.1f, 100.0f);
        glUniformMatrix4fv(shader.getLocation("projection"), false, projection.get(stack.mallocFloat(16)));

        // View Matrix
        Matrix4f view = cam.getViewMat();
        glUniformMatrix4fv(shader.getLocation("view"), false, view.get(stack.mallocFloat(16)));
        blockManager.checkDestroy();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        blockManager.draw(shader, stack);

        // Unbind shader
        shader.unbind();

        // Bind Light Shader
        lightShader.bind();

        // Set projection and view matrices
        glUniformMatrix4fv(lightShader.getLocation("projection"), false, projection.get(stack.mallocFloat(16)));
        glUniformMatrix4fv(lightShader.getLocation("view"), false, view.get(stack.mallocFloat(16)));

        // Draw light source
        light.draw(lightShader, stack);

        // Unbind light shader
        lightShader.unbind();

        renderText();
    }

    private void enabled2DContext() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, window.getWidth(), window.getHeight(), 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_LIGHTING);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
    }

    private void disable2DContext() {
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LIGHTING);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void renderText() {
        enabled2DContext();

        int sfont = 0; // font 24
        font.draw(2, 20, sfont, "Press Esc to exit mouse capture.");
        font.draw(2, 40, sfont, "Press Q to enter mouse capture.");
        font.draw(2, 60, sfont, "Press WASD to move around.");
        font.draw(2, 80, sfont, "Press Space/Shift to move up/down.");
        font.draw(2, 100, sfont, "Press 0-9 to circle options.");
        if (text != null) {
            // font 14
            font.draw(2, window.getHeight() - 20, sfont + 3, text);
        }
        disable2DContext();
    }

    public void setText(String text) {
        this.text = text;
    }

    private void init() {
        window = new Window("Visualizer", 650, 650);
        window.create();

        shader = new Shader("vertex.glsl", "fragment.glsl");
        try {
            shader.link();
        } catch (Exception e) {
            e.printStackTrace();
        }

        lightShader = new Shader("light_vertex.glsl", "light_fragment.glsl");
        try {
            lightShader.link();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cam = new Camera();
        cam.addMouseCallback(window);

        lightPos = get_light_position();
        light = new Block(BlockType.LIGHT);
        light.getModel().translate(lightPos);

        font = new Font("Karrik-Regular.ttf");

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_CULL_FACE);
        glClearColor(0.1607843137254902f, 0.6235294117647059f, 1.0f, 1.0f);

    }

    private Vector3f get_light_position(){
        return new Vector3f(blockManager.WIDTH / 2f, 20.5f, blockManager.LENGTH / 2f);
    }

    private void destroy() {
        shader.destroy();
        lightShader.destroy();
        blockManager.destroy();
        eventManager.destroy();
        window.destroy();
    }

    private void update() {
        cam.procInput(window);
    }

    public void run(boolean shouldUseOldEvents) {
        isRunning = true;
        if (!shouldUseOldEvents) {
            eventManager.destroy();
        }
        blockManager.destroy();
        init();
        while (!window.shouldClose()) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            update();

            try (MemoryStack stack = MemoryStack.stackPush()) {
                draw(stack);
            }

            window.update();
        }
        destroy();
        isRunning = false;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Visualizer() {
        blockManager = new BlockManager();
    }

    public BlockManager getBlockManager() {
        return blockManager;
    }

    public static EventManager getEventManager() {
        return eventManager;
    }
}
