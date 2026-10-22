import React, {Component} from 'react';
import {registerTableBox, processHashUrl, Navs} from 'be5-react';
import {Field, FieldNotEmpty} from './utils';

class RunTabTableBox extends Component
{
  render()
  {
    const run = this.props.value.data.attributes.rows[0];

    // The SQL sets ID to -1 when the project has no runs (LEFT JOIN runs is NULL).
    // In that case only show the "No runs" message — the other fields would
    // otherwise display the *project's* dates/who, which is invalid for a run view.
    if (run.ID.value === -1 || run.ID.value === '-1')
    {
        return (
            <div className="container">
                <Field  title='Название'            value={run.Name.value}/>
            </div>);
    }

    return (
        <div className="container">
            <Field  title='Название'            value={run.Name.value}/>
            <Field  title='Статус'              value={run.Status.value}/>
            <br/>
            <Field  title='Создано'             value={run.creationdate___.value}/>
            <Field  title='Создал'              value={run.whoinserted___.value}/>
            <FieldNotEmpty  title='Изменено'    value={run.modificationdate___.value}/>
            <FieldNotEmpty  title='Изменил'     value={run.whomodified___.value}/>
            <br/>
	    <div className="row">
              <pre>{run.Data.value}</pre>
            </div>
        </div>);
  }
}

registerTableBox('runTab', RunTabTableBox);
export default RunTabTableBox;