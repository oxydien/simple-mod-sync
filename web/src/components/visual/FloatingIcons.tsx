import {createEffect, createSignal, JSX} from "solid-js";
import Icon from "../common/Icon";

const ICON_AMOUNT = 60;

export default function FloatingIcons() {
  const iconList = ["mod", "resourcepack", "shader", "datapack", "packed", "platforms/modrinth", "platforms/curseforge"];
  const createdPrimitives: JSX.Element[] = [];
  const [primitives, setPrimitives] = createSignal<JSX.Element[]>([]);

  const createAndPlaceIcon = () => {
    let isValidPosition = false;
    let iteration = 0;
    const newPosition = { x: 0, y: 0 };

    while (!isValidPosition) {
      iteration++;
      newPosition.x = Math.floor(Math.random() * 110 - 10) + 10;
      newPosition.y = Math.floor(Math.random() * 110 - 10) + 10;

      isValidPosition = true;
      for (const primitive of createdPrimitives) {
        if (primitive && typeof primitive === "object") {
          const primitiveStyle =
            (primitive as HTMLImageElement).style || {};
          const primitiveX = Number.parseFloat(primitiveStyle.left || "0");
          const primitiveY = Number.parseFloat(primitiveStyle.top || "0");
          const distance = Math.sqrt(
            (newPosition.x - primitiveX) ** 2 +
            (newPosition.y - primitiveY) ** 2
          );

          if (distance < 30) {
            isValidPosition = false;
            break;
          }
        }
      }

      if (iteration > 60) {
        break; // Prevent infinite loop
      }
    }

    const iconName = iconList[
      Math.floor(Math.random() * iconList.length)
    ];

    return (
      <div
        style={{
          display: "inline-block",
          position: "absolute",
          top: `${newPosition.y}%`,
          left: `${newPosition.x}%`,
          "animation-delay": `${Math.random() * -2}s`,
          opacity: Math.random() * 0.5 + 0.2,
          "z-index": -1,
          "box-sizing": "revert",
        }}
      >
        <Icon name={iconName} />
      </div>
    );
  };

  const createPrimitive = () => {
    for (let i = 0; i < ICON_AMOUNT; i++) {
      createdPrimitives.push(createAndPlaceIcon());
    }
  };

  createEffect(() => {
    createPrimitive();
    setPrimitives(createdPrimitives);
  });

  return (
    <div id="floating_icons" class="object_spawner">
      {primitives()}
    </div>
  );
}