import React, { useEffect, useRef } from 'react';
import {
  registerTableBox, Navs, bus, processHashUrlForDocument
} from 'be5-react';

// The project card's tab content (samples/files/analyses/etc.) is rendered in a
// nested "navs" document (see Navs.js). The card itself is a tableBox (not a
// page), so no backend FrontendAction can re-render just the card after a modal
// operation — setUrl/redirect would replace the whole main document or re-open
// the modal. Instead we listen for the global "executeFrontendActions" bus event
// (fired by be5 after every operation result, frontendActions.js) and, if the
// user is on a tab whose table changes with operations, re-load that tab's table
// into the navs document so the change is reflected without a manual tab click.
const NAVS_DOCUMENT = "navs";
// Tabs whose table must be re-loaded after any operation result (matched by the
// tab URL, which is a stable per-tab identity). The "Files" tab: the Import
// operation adds files. The "Workflows" tab: RunWorkflow adds a workflow run,
// DeleteWorkflowRun removes one — without a reload the table keeps showing the
// stale (deleted) row and renders an error.
const REFRESH_TABS = [
  "#!table/workflow_runs/ForProjectCard/___prjID=",
  "#!table/file_info/ForProjectCard/___prjID=",
];
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

  // The navs document has no API exposing the current step, so after a tab click
  // we mirror the *URL* of the active tab here (stable identity) for the
  // post-operation refresh check below.
  const activeTabUrlRef = useRef(null);

  useEffect(() => {
    be5.ui.setTitle(title);
  }, [title]);

  useEffect(() => {
    // Fired by be5 after every operation result is processed. If the user is on
    // a tab that must reflect operation results (see REFRESH_TABS), re-load its
    // table so the change is visible without a manual tab click.
    const handler = () => {
      const url = activeTabUrlRef.current;
      if (!url || !REFRESH_TABS.includes(url)) return;
      // Defer to next tick so the modal unmount finishes first.
      setTimeout(() => processHashUrlForDocument(url, NAVS_DOCUMENT), 0);
    };
    bus.listen("executeFrontendActions", handler);
    // Note: bus has no unlisten; this listener lives for the component's mount
    // lifetime (the project card). It's idempotent (re-loads the same query).
  }, [project.ID.value]);

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
