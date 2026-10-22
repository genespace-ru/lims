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
// Tabs whose table must be re-loaded after any operation result. The "Файлы"
// tab: the Import operation adds files. The "Сценарии" tab: RunWorkflow adds a
// workflow run, DeleteWorkflowRun removes one — without a reload the table keeps
// showing the stale (deleted) row and renders an error.
const REFRESH_TABS = [3, 6]; // indices of "Сценарии" and "Файлы" in `steps`

const ProjectCardTableBox = ({ value }) => {
  const project = value.data.attributes.rows[0];
  const title = project.PageTitle.value;

  // Active tab index. Navs keeps its own internal state and doesn't expose it,
  // so we mirror it here (set on each tab click). startAtStep is kept a literal
  // 0 — a dynamic value risks an out-of-range steps[index] in Navs.init.
  const activeTabRef = useRef(0);

  useEffect(() => {
    be5.ui.setTitle(title);
  }, [title]);

  const steps = [
    { title: "Проект",   url: "#!table/projects/ProjectTab/___prjID=" + project.ID.value },
    { title: "Запуск", url: "#!table/runs/RunTab/___prjID=" + project.ID.value },
    { title: "Образцы", url: "#!table/samples/ForProjectCard/___prjID=" + project.ID.value },
    { title: "Сценарии", url: "#!table/workflow_runs/ForProjectCard/___prjID=" + project.ID.value },
    { title: "Контроль", url: "#!table/projects/ToDo/___prjID=" + project.ID.value },
    { title: "Анализы", url: "#!table/analyses/ForProjectCard/___prjID=" + project.ID.value },
    { title: "Файлы", url: "#!table/file_info/ForProjectCard/___prjID=" + project.ID.value },
  ];

  useEffect(() => {
    // Fired by be5 after every operation result is processed. If the user is on
    // a tab that must reflect operation results (see REFRESH_TABS), re-load its
    // table so the change is visible without a manual tab click.
    const handler = () => {
      if (!REFRESH_TABS.includes(activeTabRef.current)) return;
      const url = steps[activeTabRef.current] && steps[activeTabRef.current].url;
      if (!url) return;
      // Defer to next tick so the modal unmount finishes first.
      setTimeout(() => processHashUrlForDocument(url, NAVS_DOCUMENT), 0);
    };
    bus.listen("executeFrontendActions", handler);
    // Note: bus has no unlisten; this listener lives for the component's mount
    // lifetime (the project card). It's idempotent (re-loads the same query).
  }, [project.ID.value]);

  const onOpenNav = (id) => {
    activeTabRef.current = id;
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
