import {Component, createSignal, Show} from 'solid-js';
import InfoBox from "./components/info/InfoBox";
import AppHeader from "./components/nav/AppHeader";
import {links} from "./func/links";
import build from "./func/build";
import Important from "./components/info/Important";
import LandingPage from "./pages/LandingPage";
import GeneratorPage from "./pages/GeneratorPage";

const App: Component = () => {
  const [route, setRoute] = createSignal("");

  const isGenerator = location.hash.includes("generator");
  if (isGenerator) {
    setRoute("generator");
  }

  return (
    <main>
      <AppHeader />
      <Show when={build.isDevelopment}>
        <InfoBox variant="warning">
          <p>
            <Important>This page is in development!</Important><br />
            If you encounter issues, please <a href={links.issues} target="_blank" class="underline">report them on github</a>! <br />
          </p>
        </InfoBox>
      </Show>

      <Show
        when={route() === "generator"}
        fallback={<LandingPage onRoute={setRoute} />}
      >
        <GeneratorPage />
      </Show>

    </main>
  );
};

export default App;
