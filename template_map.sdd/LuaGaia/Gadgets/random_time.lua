
-- random_time.lua

--

function gadget:GetInfo ()
    return {
        name      = "Random Time",
        desc      = "Sets random light conditions on the map",
        author    = "Code_Man",
        date      = "29/4/2016",
        license   = "MIT X11",
        layer     = 1,
        enabled   = true
    }
end

if (not gadgetHandler:IsSyncedCode()) then
    return false
end

local value
local dir = false

function gadget:Initialize ()
    local mapOptions = Spring.GetMapOptions ()
    if mapOptions ~= nil and #mapOptions ~= 0 then
        if tonumber (mapOptions.weather) == 0 then
            gadgetHandler:RemoveGadget ()
        end
    end
end

function gadget:GamePreload ()
end

function gadget:GameStart ()
    value = (math.random (100) + 1) / 100
    Spring.SetSunManualControl (true)
end

function gadget:GameFrame (n)
    if n % 5 ~= 0 then
        return
    end
    if value + 0.001 >= 0.75 + math.random (25) / 100 then
        dir = true
    elseif value - 0.001 <= 0.25 - math.random (25) / 100 then
        dir = false
    end
    if dir then
        value = value - 0.001
    else
        value = value + 0.001
    end
    if math.random (20) % 19 == 0 then
        if dir then
            dir = false
        else
            dir = true
        end
    end
    --Spring.SetSunParameters (value * 100, value * 100, value * 100, 20, 50, 10)
    Spring.SetSunLighting ({groundAmbientColor = {value, value, value}})
    Spring.SetSunLighting ({groundDiffuseColor = {value, value, value}})
    Spring.SetSunLighting ({groundSpecularColor = {value, value, value}})
    Spring.SetSunLighting ({unitAmbientColor = {value, value, value}})
    Spring.SetSunLighting ({unitDiffuseColor = {value, value, value}})
    Spring.SetSunLighting ({unitSpecularColor = {value, value, value}})
    Spring.SetSunLighting ({specularExponent = value})
    Spring.SetAtmosphere ({sunColor = {value, value, value, value}})
    Spring.SetAtmosphere ({skyColor = {value, value, value, value}})
    Spring.SetAtmosphere ({cloudColor = {value, value, value, value}})
    Spring.SetAtmosphere ({fogStart = 0.25, fogEnd = 0.75, fogColor = {1.0, 1.0, 1.0, 0.5}}) -- Decoupled from map lightning
end
