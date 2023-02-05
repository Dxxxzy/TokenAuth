package studio.dreamys.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import studio.dreamys.TokenAuth;

import java.awt.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class SessionGui extends GuiScreen {
    private GuiScreen previousScreen;

    private String status = "Session:";
    private GuiTextField sessionField;
    private ScaledResolution sr;

    public SessionGui(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        sr = new ScaledResolution(mc);

        sessionField = new GuiTextField(1, mc.fontRendererObj,sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2, 200, 20);
        sessionField.setMaxStringLength(32767);
        sessionField.setFocused(true);

        buttonList.add(new GuiButton(998, sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2 + 30, 200, 20, "Login"));
        buttonList.add(new GuiButton(999, sr.getScaledWidth() / 2 - 100, sr.getScaledHeight() / 2 + 60, 200, 20, "Restore"));

        super.initGui();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);

        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        mc.fontRendererObj.drawString(status, sr.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(status) / 2, sr.getScaledHeight() / 2 - 30, Color.WHITE.getRGB());
        sessionField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        //login button
        if (button.id == 998) {
            try {
                String username, uuid, token, session = sessionField.getText();

                if (session.contains(":")) { //if fully formatted string (ign:uuid:token)
                    //split string to data
                    username = session.split(":")[0];
                    uuid = session.split(":")[1];
                    token = session.split(":")[2];
                } else { //if only token
                    //make request
                    HttpURLConnection c = (HttpURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile/").openConnection();
                    c.setRequestProperty("Content-type", "application/json");
                    c.setRequestProperty("Authorization", "Bearer " + sessionField.getText());
                    c.setDoOutput(true);

                    //get json
                    JsonObject json = new JsonParser().parse(IOUtils.toString(c.getInputStream())).getAsJsonObject();

                    //get data
                    username = json.get("name").getAsString();
                    uuid = json.get("id").getAsString();
                    token = session;
                }

                //set session and return to previous screen
                mc.session = new Session(username, uuid, token, "mojang");
                mc.displayGuiScreen(previousScreen);
            //in case we couldn't set session for some reason
            } catch(Exception e) {
                status = "§cError: Couldn't set session (check mc logs)";
                e.printStackTrace();
            }
        }

        //restore button
        if (button.id == 999) {
            try {
                mc.session = TokenAuth.originalSession;
                mc.displayGuiScreen(previousScreen);
            } catch (Exception e) {
                status = "§cError: Couldn't restore session (check mc logs)";
                e.printStackTrace();
            }
        }

        super.actionPerformed(button);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        sessionField.textboxKeyTyped(typedChar, keyCode);

        if (Keyboard.KEY_ESCAPE == keyCode) mc.displayGuiScreen(previousScreen);
        else super.keyTyped(typedChar, keyCode);
    }
}
