import React, { useEffect, useRef } from 'react';
import {
  registerTableBox, Navs, bus
} from 'be5-react';

// The project card's tab content (samples/files/analyses/etc.) is rendered in a
// nested "navs" document (see Navs.js).
const NAVS_DOCUMENT = "navs";
// The server builds the card's `Tabs` cell: each tab is "{{{LocalizedKey}}}|<url>",
// tabs joined by "||". The {{...}} markers are resolved server-side (ru.yaml), so
// by the time JS reads the cell the titles are already localized.
const Tabs = (cell) => (cell && cell.value ? cell.value : "")
  .split("||")
  .map((s) => s.split("|"))
  .filter((p) => p.length === 2)
  .map((p) => ({ title: p[0], url: p[1] }));

const ProjectCardTableBox = ({ value }) => {
  const project = value.data.attributes.rows[0];
  const title = project.PageTitle.value;
  const steps = Tabs(project.Tabs);

  // The navs document has no API exposing the current step, so we mirror the
  // *URL* of the active tab here (stable identity) for the post-operation
  // refresh check below. Navs.init() auto-loads steps[startAtStep] on mount
  // WITHOUT calling onOpenNav, so seed the ref from that same default —
  // otherwise it stays null until the user explicitly clicks a tab, and the
  // refresh either no-ops or re-fetches a URL that may lack the project ID.
  const activeTabUrlRef = useRef(steps[0] ? steps[0].url : null);

  // The server builds each tab URL carrying ___prjID=<id> (see the `Tabs` cell
  // in the `Project card` query, projects.yaml), but when be5 loads the table
  // it rewrites the document's `links.self` via FilterUtil.getContextParams,
  // which strips every parameter starting with `_` — so `___prjID` is dropped
  // from `links.self`. A later be5 Document.refresh() (fired by
  // REFRESH_PARENT_DOCUMENT after a modal operation) re-requests
  // `links.self`, producing an empty-`___prjID` request → malformed
  // `WHERE wr.project = ORDER BY …` (Postgres 42601). Patch `links.self` back
  // to the active tab's full URL (which carries ___prjID) so the refresh
  // re-requests the correct URL. The Document's own change handler stores
  // `data.value` by reference, so mutating the shared object here is enough.
  useEffect(() => {
    const handler = (data) => {
      if (!data || !data.value || !data.value.data) return;
      const links = data.value.data.links;
      if (!links) return;
      const self = links.self;
      if (!self || /___prjID=\d+$/.test(self)) return;
      // Only patch if the active tab is the same query that just loaded (so we
      // don't yank a non-ForProjectCard tab to a ForProjectCard URL). The tab
      // URL is `#!<base>/___prjID=<id>`; `links.self` is the server-built
      // `<base>` (no `#!`, no params — FilterUtil strips `_`-prefixed keys).
      // Compare the tab URL's base (with `#!` stripped) against `self`.
      const url = activeTabUrlRef.current;
      if (!url) return;
      // Strip the `#!` prefix: the tab URL is `#!<base>/___prjID=<id>`, and
      // `links.self` must be `<base>/___prjID=<id>` (no `#!`) so that
      // Document.refresh()'s `"#!" + getSelfUrl(...)` produces a valid URL.
      const selfUrl = url.replace(/^#!/, "");
      const m = selfUrl.match(/^(.*)\/___prjID=\d+$/);
      if (!m || m[1] !== self) return;
      links.self = selfUrl;
    };
    bus.listen(NAVS_DOCUMENT, handler);
  }, []);

  useEffect(() => {
    be5.ui.setTitle(title);
  }, [title]);

  const onOpenNav = (id) => {
    const step = steps[id];
    activeTabUrlRef.current = step ? step.url : null;
  };

  return (
    <div className="projectInfo">
      <h1>{title}</h1>
      <Navs
        steps={steps}
        tabs
        startAtStep={0}
        onOpenNav={onOpenNav}
        documentName={NAVS_DOCUMENT}
      />
    </div>
  );
};

registerTableBox('projectCard', ProjectCardTableBox);

export default ProjectCardTableBox;
