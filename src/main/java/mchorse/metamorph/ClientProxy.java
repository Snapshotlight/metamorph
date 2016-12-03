package mchorse.metamorph;

import java.lang.reflect.Field;
import java.util.Map;

import mchorse.metamorph.client.KeyboardHandler;
import mchorse.metamorph.client.RenderingHandler;
import mchorse.metamorph.client.gui.GuiMenu;
import mchorse.metamorph.client.gui.GuiOverlay;
import mchorse.metamorph.client.render.RenderMorph;
import mchorse.metamorph.client.render.RenderPlayer;
import mchorse.metamorph.client.render.RenderSubPlayer;
import mchorse.metamorph.entity.EntityMorph;
import mchorse.vanilla_pack.VanillaPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client proxy
 * 
 * Client proxy is responsible for adding some rendering modifications (i.e. 
 * HUD morph panel and player rendering) and also responsible for loading 
 * (constructing ModelCustom out of) custom models. 
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    /**
     * GUI menu which is responsible for choosing morphs 
     */
    public static GuiMenu overlay = new GuiMenu();

    /**
     * GUI overlay which is responsible for showing up acquired morphs
     */
    public static GuiOverlay morphOverlay = new GuiOverlay();

    @Override
    public void preLoad(FMLPreInitializationEvent event)
    {
        super.preLoad(event);

        RenderingRegistry.registerEntityRenderingHandler(EntityMorph.class, new RenderMorph.MorphFactory());
    }

    @Override
    public void load()
    {
        super.load();

        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        RenderPlayer render = new RenderPlayer(manager, 0.5F);

        MinecraftForge.EVENT_BUS.register(new RenderingHandler(overlay, render, morphOverlay));
        MinecraftForge.EVENT_BUS.register(new KeyboardHandler(overlay));

        this.substitutePlayerRenderers(render);
    }

    /**
     * Load custom models.
     * 
     * This code is responsible for assembling client custom models out of 
     * already parsed data models. 
     */
    @Override
    public void loadModels()
    {
        super.loadModels();

        VanillaPack.loadClientModels(this.models);
    }

    /**
     * Substitute default player renders to get the ability to render the
     * hand.
     *
     * Please, kids, don't do that at home. This was made by an expert in
     * this field, so please, don't override skinMap the way I did. Don't break
     * the compatibility with this mod.
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void substitutePlayerRenderers(RenderPlayer render)
    {
        RenderManager manager = Minecraft.getMinecraft().getRenderManager();
        Map<String, net.minecraft.client.renderer.entity.RenderPlayer> skins = null;

        /* Iterate over all render manager fields and get access to skinMap */
        for (Field field : manager.getClass().getDeclaredFields())
        {
            if (field.getType().equals(Map.class))
            {
                field.setAccessible(true);

                try
                {
                    Map map = (Map) field.get(manager);

                    if (map.get("default") instanceof net.minecraft.client.renderer.entity.RenderPlayer)
                    {
                        skins = map;

                        break;
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }

        /* Replace player renderers with Blockbuster substitutes */
        if (skins != null)
        {
            skins.put("slim", new RenderSubPlayer(manager, render, true));
            skins.put("default", new RenderSubPlayer(manager, render, false));

            System.out.println("Skin map renderers were successfully replaced with Blockbuster RenderSubPlayer substitutes!");
        }
    }
}