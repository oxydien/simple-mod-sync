import Button from "../common/Button";
import {links} from "../../func/links";
import Icon from "../common/Icon";

export default function AppHeader() {
  return (
    <header class="hidden md:flex flex-row justify-between items-center gap-2 border-b-sep py-1 px-4 mb-6">
      <a href="/">
        <img src="/assets/banner.png" alt="Simple Mod Sync" class="app-header" />
      </a>
      <menu class="flex gap-1 list-none">
        <li>
          <Button variant="default" link={links.github}>
            Github
          </Button>
        </li>
        <li>
          <Button variant="primary" link={links.sms_modrinth}>
            <Icon name="platforms/modrinth" />
            Get on Modrinth
          </Button>
        </li>
      </menu>
    </header>
  )
}