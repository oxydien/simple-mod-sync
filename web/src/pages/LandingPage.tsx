import Important from "../components/info/Important";
import Icon from "../components/common/Icon";
import Button from "../components/common/Button";
import {links} from "../func/links";
import Separator from "../components/common/Separator";
import FloatingIcons from "../components/visual/FloatingIcons";


interface LandingPageProps {
  onRoute?: (route: string) => void;
}

export default function LandingPage(props: LandingPageProps) {

  return (<>
    <FloatingIcons />

    <div class="flex flex-col gap-2 my-20 mx-auto w-fit items-center text-center relative">
      <h1 class="text-5xl font-bold">
        Simple Mod Sync
      </h1>
      <strong class="text-primary text-xl">
        Let's make content synchronization easy!
      </strong>

      <small class="max-w-120">
        Simple Mod Sync lets you create a list of mods and other content that can be automatically downloaded and updated. Instead of telling your friends "download these 20 mods from different websites," you give them one link and this mod does the rest!
      </small>

      <div class="flex flex-wrap gap-2 pt-8">
        <Button class="w-[initial]! grow" link={links.github}>
          Source code!
        </Button>
        <Button class="w-[initial]! grow" variant="primary" link={links.sms_modrinth}>
          Get it now!
        </Button>
      </div>
    </div>

    <Separator class="mt-20">
      Tools
    </Separator>
    <section id="tools" class="flex flex-wrap justify-center gap-2">
      <Button
        class="flex-col max-w-70"
        variant="primary"
        onClick={() => props.onRoute && props.onRoute("generator")}
      >
        <Important>
          Schema Generator
        </Important>
        <small>
          Lets you select mods from various platforms and automatically generates the Sync Schema file for you. <br /><br />
          You can also import an existing JSON schema to edit it.
        </small>
      </Button>
      <Button
        class="flex-col max-w-70"
        link={links.translators}
      >
        <Important>
          Translators
        </Important>
        <small>
          A collection of scripts that translate modpacks from various formats to the Simple Mod Sync format.
        </small>
      </Button>
      <Button
        class="flex-col max-w-70"
        link={links.docs}
      >
        <Important>
          Documentation
        </Important>
        <small>
          Guide and documentation of how the Simple Mod Sync format works with advanced features and examples.
        </small>
      </Button>
    </section>

    <Separator class="mt-20">
      What does it offer?
    </Separator>
    <section id="features" class="flex flex-col items-center gap-8">
      <div class="sect max-w-160">
        <Important>
          <Icon class="inline mr-2" name="downloading" />
          Synchronization
        </Important>

        <p>
          Downloads mods and other content from various platforms
          (<Icon class="inline mx-1" name="platforms/modrinth" />, <Icon class="inline mx-1" name="platforms/curseforge" />),
          CDNs, or URLs.
        </p>

        <strong>Supported content types:</strong>
        <ul class="list-['-'] list-inside">
          <li><span class="pl-2">Mods</span></li>
          <li><span class="pl-2">Resource packs</span></li>
          <li><span class="pl-2">Shaders</span></li>
          <li><span class="pl-2">Configs (any zipped content)</span></li>
          <li><span class="pl-2">Data packs</span></li>
        </ul>

        <img
          src="/assets/screens/content-overview.png"
          alt="Content overview screen"
          class="mt-2"
        />
      </div>

      <div class="sect max-w-160">
        <Important>
          <Icon class="inline mr-2" name="error" />
          Runs in the background
        </Important>

        <p>
          When the game launches, it automatically starts the synchronization process. <br />
          If anything goes wrong, you are notified directly within the user interface.
        </p>

        <img
          src="/assets/screens/settings.png"
          alt="Settings screen"
          class="mt-2"
        />
      </div>

      <div class="sect max-w-160">
        <Important>
          <Icon class="inline mr-2" name="modified" />
          Smart Notifications
        </Important>

        <p>
          If content has been updated when you try to play, you will be prompted to restart the game before joining.
        </p>
        <img
          src="/assets/screens/game-update.png"
          alt="Game requires restart popup"
          class="mt-2"
        />
      </div>
    </section>

    <Separator class="mt-20">
      Need help?
    </Separator>
    <section id="help" class="mb-12 sect max-w-160 mx-auto">
      <Important>Contact me!</Important>
      <p>
        Whether it's a technical issue, or a feature isn't behaving as described <span class="whitespace-nowrap">-&gt;</span>&nbsp;
        <a class="underline" href={links.issues}>Report an issue on GitHub <Icon class="inline" name="external" /></a>
      </p>
    </section>

  </>)
}